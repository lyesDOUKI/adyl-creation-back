#!/bin/bash

set -e

echo "================================="
echo " Starting Adyl Création stack"
echo "================================="


echo ""
echo "1) Creating Docker network..."
./docker-network.sh


echo ""
echo "2) Starting PostgreSQL..."
cd postgres
docker compose -p postgres-adyl-creation up -d
cd ../..

echo ""
echo "3) Starting Backend API..."

docker compose up -d



echo ""
echo "Waiting for infrastructure startup..."
sleep 10


echo ""
echo "================================="
echo " Adyl Création started"
echo "================================="
