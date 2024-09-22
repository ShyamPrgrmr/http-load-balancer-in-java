#!/bin/bash

image="shyamya/simple-http-server:latest"
network="load-balancer-network"

echo "Pulling image:$image if not exists"

docker pull $image

echo "Creating docker network if not exists"

docker network create $network

echo "Running test server (Running three instances - default) Edit this file if needed more"

docker run --rm -d --name simple-http-machine-1 --network $network -e SERVER_NAME="MACHINE_1" -e S_PORT="3000" -p 3001:3000 simple-http-server
docker run --rm -d --name simple-http-machine-2 --network $network -e SERVER_NAME="MACHINE_2" -e S_PORT="3000" -p 3002:3000 simple-http-server
docker run --rm -d --name simple-http-machine-3 --network $network -e SERVER_NAME="MACHINE_3" -e S_PORT="3000" -p 3003:3000 simple-http-server


echo "Containers : "

docker ps | grep "simple-http-machine"
