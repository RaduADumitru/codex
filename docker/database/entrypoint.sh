#!/bin/sh

apk add ca-certificates wget
wget --version
wget --quiet --tries=10 --waitretry=40 http://localhost:8529
# arangod config "arangod.conf"
arangorestore \
--server.endpoint "tcp://localhost:8529"
--input-directory "/dump" \
--create-collection true \
--create-database true \
--overwrite true \
--server.username "root" \
--server.password "openSesame" \
--server.database "dex" 
