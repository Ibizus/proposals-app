#!/bin/bash

MYSQL_WAIT_TIMEOUT=${MYSQL_WAIT_TIMEOUT:-120}

usage() {
    echo "Usage: ./dev.sh [--reset]"
    echo ""
    echo "Starts MySQL and jsreport in Docker, then the backend (dev profile) and the frontend."
    echo ""
    echo "Options:"
    echo "  --reset    Remove the project's Docker containers and MySQL volume first, so the"
    echo "             backend starts on an empty database and seeds fresh demo data."
    echo "  -h, --help Show this help."
}

RESET=false
for arg in "$@"; do
    case "$arg" in
        --reset) RESET=true ;;
        -h|--help) usage; exit 0 ;;
        *) echo "❌ Unknown option: $arg"; echo ""; usage; exit 1 ;;
    esac
done

# Cleanup running background processes and containers on exit
cleanup() {
    local status=$?
    echo ""
    echo "🛑 Stopping local servers and Docker containers..."
    kill $(jobs -p) 2>/dev/null
    docker compose stop mysql jsreport
    exit "$status"
}

# Install frontend dependencies when node_modules is missing or package-lock.json changed
ensure_front_deps() {
    local hash_file="front/node_modules/.lock-hash"
    local lock_hash
    lock_hash=$(sha256sum front/package-lock.json | cut -d' ' -f1)

    if [ -d front/node_modules ] && [ -f "$hash_file" ] && [ "$(cat "$hash_file")" = "$lock_hash" ]; then
        echo "✅ Frontend dependencies are up to date."
        return 0
    fi

    echo "📦 Installing frontend dependencies (npm ci)..."
    if (cd front && npm ci); then
        echo "$lock_hash" > "$hash_file"
    else
        echo "❌ npm ci failed in front/. Skipping Frontend; fix the install and re-run ./dev.sh."
        return 1
    fi
}

# Wait until the mysql service reports healthy (healthcheck in docker-compose.yml)
wait_for_mysql() {
    local container status waited=0
    container=$(docker compose ps -q mysql)
    if [ -z "$container" ]; then
        echo "❌ The mysql container is not running. Check 'docker compose ps' and 'docker compose logs mysql'."
        return 1
    fi

    echo "⏳ Waiting for MySQL to be healthy (timeout ${MYSQL_WAIT_TIMEOUT}s)..."
    while true; do
        status=$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container" 2>/dev/null)
        case "$status" in
            healthy)
                echo "✅ MySQL is ready (${waited}s)."
                return 0
                ;;
            # "unhealthy" can clear once a slow first initialization finishes, so keep waiting
            exited|dead)
                echo "❌ The mysql container is $status. Last log lines:"
                docker compose logs --tail 20 mysql
                return 1
                ;;
        esac
        if [ "$waited" -ge "$MYSQL_WAIT_TIMEOUT" ]; then
            echo "❌ MySQL was not healthy after ${MYSQL_WAIT_TIMEOUT}s (status: ${status:-unknown}). Check 'docker compose logs mysql'."
            return 1
        fi
        sleep 2
        waited=$((waited + 2))
    done
}

# Load environment variables from .env file if it exists
if [ -f .env ]; then
  set -o allexport
  source .env
  set +o allexport
fi

if [ "$RESET" = true ]; then
    echo "🧹 Removing Docker containers and the MySQL volume (--reset)..."
    if ! docker compose down --volumes --remove-orphans; then
        echo "❌ Reset failed; nothing was started."
        exit 1
    fi
fi

trap cleanup INT TERM EXIT

echo "🚀 Starting Docker infrastructure..."
if ! docker compose up -d mysql jsreport; then
    echo "❌ docker compose up failed. If it reports a container name conflict, a container"
    echo "   with that name belongs to another compose project (for example an older checkout"
    echo "   in a differently named folder): remove it with 'docker rm -f <name>' and re-run."
    exit 1
fi

wait_for_mysql || exit 1

# Ensure execution permissions for Maven wrapper
chmod +x back/mvnw 2>/dev/null

echo "🔥 Starting Backend (Spring Boot)..."
# The dev profile seeds demo data into an empty database (see back/src/main/resources/application-dev.properties)
(cd back && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev) &

if ensure_front_deps; then
    echo "🎨 Starting Frontend (Angular)..."
    (cd front && npx ng serve --open=false) &
fi

# Keep script active to display logs and handle shutdown
wait