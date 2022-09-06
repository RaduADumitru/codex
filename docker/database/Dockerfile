FROM arangodb:3.9.2
ADD ../../dump/ /dump/
RUN "sudo docker exec arango-tsv arangorestore \
--input-directory "/dump" \
--create-collection true \
--create-database true \
--overwrite true \
--server.username "root" \
--server.password "openSesame" \
--server.database "dex" "
