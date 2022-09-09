FROM arangodb:3.9.2
ADD ./dump/ /dump/
ADD entrypoint.sh ./entrypoint.sh
ADD server.conf /server.conf
RUN chmod +x entrypoint.sh
RUN ./entrypoint.sh
