# codex
Web API with performance tests for searches in DEXonline graph database

As of now, the repo contains a Spring Boot app for the API, and a dump of the [DEXonline database](https://github.com/dexonline/dexonline/wiki/Database-Schema), migrated from SQL to ArangoDB. To load it into ArangoDB, use ArangoRestore in the ArangoDB shell. An example:

`arangorestore --server.username {username} --server.password {password} --server.database dex --create-database true --input-directory "dump"`

## Current API endpoints:
* `/lexeme/levenshtein`: POST - parameters `word`, `dist` - returns all words with a Levenshtein distance of maximum `dist` from `word`
* `/lexeme/regex`: POST - parameter `regex` - returns all words matching the Regex expression `regex`
## TODO: 
* Configure Swagger for documentation
* More endpoints / searches
* Add Performance tests
* Dockerization
