#! /bin/bash

if [ -d "./target/" ]
then
    rm -r target/
fi
docker build -t build-jar-inside-docker-image .
docker create -it --name build-jar-inside-docker build-jar-inside-docker-image bash
docker cp build-jar-inside-docker:/target ./target
docker rm -f build-jar-inside-docker
