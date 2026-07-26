#!/bin/bash

NETWORK_NAME="adyl-creation-network"

if docker network ls --format '{{.Name}}' | grep -q "^${NETWORK_NAME}$"; then
    echo "Docker network '${NETWORK_NAME}' already exists."
else
    echo "Creating Docker network '${NETWORK_NAME}'..."
    docker network create "${NETWORK_NAME}"
    echo "Docker network '${NETWORK_NAME}' created successfully."
fi