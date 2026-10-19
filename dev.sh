#!/bin/bash

# Cleanup running background processes and containers on exit
cleanup() {
    echo ""
    echo "🛑 Stopping local servers and Docker containers..."
    kill $(jobs -p) 2>/dev/null
    docker compose stop mysql jsreport
    exit
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

# Load environment variables from .env file if it exists
if [ -f .env ]; then
  set -o allexport
  source .env
  set +o allexport
fi

trap cleanup INT TERM EXIT

echo "🚀 Starting Docker infrastructure..."
docker compose up -d mysql jsreport

echo "⏳ Waiting 5 seconds for MySQL to be ready..."
sleep 5

# Ensure execution permissions for Maven wrapper
chmod +x back/mvnw 2>/dev/null

echo "🔥 Starting Backend (Spring Boot)..."
(cd back && ./mvnw spring-boot:run) &

if ensure_front_deps; then
    echo "🎨 Starting Frontend (Angular)..."
    (cd front && npx ng serve --open=false) &
fi

# Keep script active to display logs and handle shutdown
wait