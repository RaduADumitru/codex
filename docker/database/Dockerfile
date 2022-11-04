FROM arangodb:latest

# to be used in healthcheck
RUN apk --update --no-cache add curl
# CMD["arangod", "--ttl.frequency", "0", "transaction.streaming-idle-timeout", "600"]
CMD ["arangod", "--transaction.streaming-idle-timeout", "120"]
