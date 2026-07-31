#!/bin/bash

set -e

echo "================================="
echo " Deploying Backend API"
echo "================================="

echo ""
echo "Stopping current backend container..."
docker compose down

echo ""
echo "Building new backend image..."
docker compose build

echo ""
echo "Starting backend..."
docker compose up -d

echo ""
echo "================================="
echo " Backend deployed successfully"
echo "================================="