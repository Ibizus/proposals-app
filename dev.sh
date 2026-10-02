#!/bin/bash

# Cleanup running background processes and containers on exit
cleanup() {
    echo ""
    echo "🛑 Stopping local servers and Docker containers..."
    kill $(jobs -p) 2>/dev/null
    docker compose stop mysql jsreport
    exit
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

echo "🎨 Starting Frontend (Angular)..."
(cd front && npx ng serve --open=false) &

# Keep script active to display logs and handle shutdown
wait