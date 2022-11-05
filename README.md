# codex
This Web API allows for migrating the database of DEXonline (website which serves as an online Romanian dictionary: https://dexonline.ro), through SQL parsing of a remote initialization script (hosted at https://dexonline.ro/static/download/dex-database.sql.gz) into an ArangoDB graph database. Additionally, through the API, a number of searches can be performed inside the dictionary.

For startup, you can use the corresponding Docker Compose service. For example: `sudo docker compose build`, then `sudo docker compose up`.
## Import
The import can be achieved in two phases, defined by the schema files `codex/src/main/resources/import-schema.json` and `codex/src/main/resources/final-schema.json`, which describe the structure of the database in their respective stages, for specification and validation purposes. The first stage is meant to simulate the structure of the original SQL database, with searches being able to be executed in a similar manner as in SQL. On the other hand, the second represents an optimized and more compact version built off of the first, for more efficient searches. 

These documents contain three root fields: `collections`, containing descriptions of document collections (analogous to SQL tables), `edgeCollections`, which describe relationships between documents in collections (similar to many-to-many SQL tables), and `generatedEdgeCollections` (edge collections generated automatically, meant to simulate SQL one-to-many relationships, and built using attributes of the "child" collection `attributeCollection`). Only the collections specified inside these fields will be imported.

Each document collection and edge collection is mapped to an [ArangoDB Schema validation object](https://www.arangodb.com/docs/stable/data-modeling-documents-schema-validation.html) (at `collections.(name)` and `edgeCollections.(name).schema` respectively). Its `rule` property describes a [JSONschema object](https://json-schema.org/learn/getting-started-step-by-step.html) against which all documents imported into the collection will be validated. When importing into the corresponding collection, only the specified properties (SQL columns) of a document will be imported, besides base properties (`_key` (primary key) for document collections, and `_from`, `_to` (equivalent to foreign keys of many-to-many tables) for edge collections). ArangoDB data types of document fields will be predetermined based on their corresponding SQL column data type.

To avoid idle timeouts resulting from excessively large transactions, the import can be paginated, so that certain large queries will be split into smaller ones, leading to increased stability.

To import the database into ArangoDB, use the following endpoint:
* `codex/import/import`: POST - parameters `boolean complete` (whether to execute second phase of import, or only the first), `integer pageCount` (number of small subqueries to split large queries into - minimum 10 recommended) - returns the string `Import complete` on a success

A partial import (at only the first stage) can also be led into the second using the endpoint:
* `codex/import/optimize`: POST - parameter `integer pageCount` - returns the string `Import complete` on a success

## Search Endpoints
Endpoints for searches have a number of similar fields: `wordform` represents the form to search against (either `accent` for accented forms, or `noaccent` for forms without accent), `relationtype` represents a relationship between two words (`synonym`, `antonym`, `diminutive` or `augmentative`)
### Endpoints usable for any stage of import
* `codex/search/levenshtein`: POST - parameters `String word`, `Integer distance`, `String wordform` - returns array of words with a Levenshtein distance of maximum `dist` from `word`, using the specified `wordform`
* `codex/search/regex`: POST - parameter `String regex`, `String wordform` - returns array of strings representing words matching the Regex expression `regex` using specified `wordform`
* `codex/knn/editdistance`: POST - parameters `String word`, `String wordform`, `String distancetype` (either `levenshtein`, `hamming` or `lcs_distance`), `Integer neighborcount` - returns array of strings representing K nearest neighbors using given `distancetype`
* `codex/knn/ngram`: POST - parameters `String word`, `String wordform`, `String distancetype` (either `ngram_similarity` or `ngram_positional_similarity`), `String neighborcount`, `String ngramsize` - returns array of strings representing K nearest neighbors using given N-gram `distancetype` and given `ngramsize`
### Endpoints only for optimized import
* `codex/optimizedsearch/meanings`: POST - parameters `String word`, `String wordform` - returns array of strings representing meanings of `word`
* `codex/optimizedsearch/etymologies`: POST - parameters `String word`, `String wordform` - returns array of objects representing etymologies of `word`. These objects represent pairings of the etymology's original word, and a tag describing its origin (language), both represented as strings.
* `codex/optimizedsearch/usageexamples`: POST - parameters `String word`, `String wordform` - returns array of strings representing usage example phrases containing `word`
* `codex/optimizedsearch/relation`: POST - parameters `String word`, `String relationtype`, `String wordform` - returns array of strings representing words with given `relationtype` to `word`
### Endpoints only for initial import
* `codex/search/meanings`: POST - parameters `String word`, `String meaningtype` (`proper_meaning`, `etymology`, `usage_example`, `comment`, `diff`, `compound_meaning`), `String wordform` - returns array of strings representing "meanings" with given `meaningtype` of `word`
* `codex/search/relation`: POST - parameters `String word`, `String relationtype`, `String wordform` - returns array of strings representing words with given `relationtype` to `word`
### Sandbox/Testing endpoints
* `codex/system/schema/collection`: GET - returns array of collections in database
* `codex/system/schema/key_types`: POST - parameter `String collection` - for each key in collection, returns types of values: response represented as array of key-type pairs
* `codex/system/collection/is_edge_collection`: POST - parameter `String collection` - returns a boolean value representing whether collection is edge collection
* `codex/system/collection/edge_relations`: POST - parameter `String collection` - return an array of string pairs, representing each pair of collections connected in specified edge collection
* `codex/system/schema/key_types_all`: GET - returns key types of all collections, as a JSON object in a format of `collection: key_types`, for each collection, as in `key_types` endpoint
* `codex/system/schema/edge_relations_all`: GET - returns edge relations of all collections, as a JSON object in format of `collection: edge_relations` for each collection, as in `edge_relations` endpoint
* `codex/system/schema/schema`: GET - documents and returns schema of database, in format `{keyTypeMap: key_types_all, edgeRelationsMap: edge_relations_all}`, as in previous two requests
* `codex/import/version`: GET - returns ArangoDB database version

### Known issues/limitations
* `"java.io.IOException: Reached the end of the stream"` error: caused by an exceedingly large transaction surpassing [ArangoDB's stream transaction idle timeout](https://www.arangodb.com/docs/stable/transactions-stream-transactions.html). The default timeout is 60 seconds, and this is mitigated somewhat by setting the server option `--transaction.streaming-idle-timeout` to the maximum of 120 seconds in the database's Dockerfile. Nevertheless, ArangoDB is not built with large transactions in mind, so it is recommended to split large transactions into smaller ones, such as by increasing the `pageCount` when importing.
## TODO:
* performance tests
