#!/bin/sh
set -e

echo "Running database migrations..."
node node_modules/prisma/build/index.js migrate deploy

echo "Seeding database (if needed)..."
node node_modules/tsx/dist/cli.mjs prisma/seed.ts || echo "Seed skipped or already applied"

echo "Starting server..."
exec node server.js
