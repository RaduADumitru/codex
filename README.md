# codex
This Web API allows for a number of searches in the DEXonline database, using the graph database ArangoDB.

As of now, the repo contains a Java Spring Boot Maven app for the API, and a dump of the [DEXonline database](https://github.com/dexonline/dexonline/wiki/Database-Schema), migrated from SQL to ArangoDB. The filedump will be loaded through the project's Docker Compose service, and is only meant as a temporary method until import through SQL parsing of database initialization script is implemented.
To start up, you can use the corresponding Docker Compose service, which will also start an import through the filedump. For example: `sudo docker compose build`, then `sudo docker compose up`

Connection details can be configured in `codex/src/main/java/org/example/codex/CodexConfiguration.java`, and schema for partial and final imports can be modified in `codex/src/main/resources/import-schema` and `codex/src/main/resources/final-schema` respectively.
## Current API endpoints:
* `codex/lexeme/levenshtein`: POST - parameters `String word`, `Integer dist`, `Integer collation` - returns array of words with a Levenshtein distance of maximum `dist` from `word` using specified `collation` (1 - UTF-8, 2 - with accent)
* `codex/lexeme/regex`: POST - parameter `String regex`, `Integer collation` - returns array of words matching the Regex expression `regex` using specified `collation` (1 - UTF-8, 2 - with accent)
* `codex/lexeme/meanings`: POST - parameter `String word`, `Integer type`, `Integer collation` - returns array of meanings of `word` with given `type` (0 - proper meaning, 1 - etymology, 2 - usage example from literature, 3 - comment, 4 - comment or diff from parent meaning, 5 - meaning of compound expression containing word
* `codex/lexeme/relation`: POST - parameter `String word`, `Integer relationType`, `Integer collation` - returns array of words with given `relationType` to `word` (1 - synonyms, 2 - antonyms, 3 - diminutives, 4 - augmentatives)
* `codex/system/schema/collection`: GET - returns array of collections in database
* `codex/system/schema/key_types`: POST - parameter `String collection` - for each key in collection, returns types of values: response represented as array of key-type pairs
* `codex/system/collection/is_edge_collection`: POST - parameter `String collection` - returns a boolean value representing whether collection is edge collection
* `codex/system/collection/edge_relations`: POST - parameter `String collection` - return an array of string pairs, representing each pair of collections connected in specified edge collection
* `codex/system/schema/key_types_all`: GET - returns key types of all collections, as a JSON object in a format of `collection: key_types`, for each collection, as in `key_types` endpoint
* `codex/system/schema/edge_relations_all`: GET - returns edge relations of all collections, as a JSON object in format of `collection: edge_relations` for each collection, as in `edge_relations` endpoint
* `codex/system/schema/schema`: GET - documents and returns schema of database, in format `{keyTypeMap: key_types_all, edgeRelationsMap: edge_relations_all}`, as in previous two requests
* `codex/import/import`: import database through SQL parsing of remote SQL script (WIP)

For all endpoints using the parameter `collation`: 1 represents UTF-8, 2 represents accented forms
## TODO:
* finalize import through SQL parsing
* performance tests
