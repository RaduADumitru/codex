FROM arangodb:3.9.2
ADD ./dump/ /dump/
#ADD entrypoint.sh /entrypoint.sh
# ADD arangod.conf /arangod.conf
# RUN chmod +x entrypoint.sh && /entrypoint.sh
CMD ["/bin/sh", "/entrypoint.sh"]

