#!/bin/bash

echo "Stopping Adyl Création stack..."

cd infra/postgres || exit
docker compose -p postgres-adyl-creation down
cd ../..

docker compose down
echo "Stopped."