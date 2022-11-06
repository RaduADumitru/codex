# codex
This Web API allows for migrating the database of the online Romanian Dictionary [DEXonline](https://dexonline.ro), through SQL parsing of a remote initialization [script](https://dexonline.ro/static/download/dex-database.sql.gz) into an ArangoDB graph database. Additionally, through the API, a number of searches can be performed inside the dictionary.

For startup, you can use the corresponding Docker Compose service. For example: `sudo docker compose build`, then `sudo docker compose up`.
## Import
The import can be achieved in two phases, defined by the schema files `codex/src/main/resources/import-schema.json` and `codex/src/main/resources/final-schema.json`, which describe the structure of the database in their respective stages, for specification and validation purposes. The first stage is meant to simulate the structure of the original SQL database, with searches being able to be executed in a similar manner as in SQL. On the other hand, the second represents an optimized and more compact version built off of the first, for more efficient searches. 

For each of these import phases, specified tables and attributes present in the SQL script will be imported (SQL database documentation is available [here](https://github.com/dexonline/dexonline/wiki/Database-Schema)).

The schema documents contain root fields describing three types of collections: 
* `collections`, containing descriptions of document collections (analogous to SQL tables)
* `edgeCollections`, which describe relationships between documents in collections (similar to many-to-many SQL tables)
* `generatedEdgeCollections`: edge collections generated automatically, meant to simulate SQL one-to-many relationships, and built using attributes of the "child" collection `attributeCollection`. These generated collections are only usable in the first phase of the import, to be used during the second phase and then deleted after import is finished. 

Only the collections and attributes specified inside these fields will be imported. Collections and attributes can be specified only in the original import schema (in which case they will be used during the optimization phase, and then deleted), or specified in both schema files, in which case they will be preserved after optimization.

Inside the schema specification files, each document collection and edge collection is mapped to an [ArangoDB Schema validation object](https://www.arangodb.com/docs/stable/data-modeling-documents-schema-validation.html) (at `collections.(name)` and `edgeCollections.(name).schema` respectively). Its `rule` property describes a [JSONschema object](https://json-schema.org/learn/getting-started-step-by-step.html) against which all documents imported into the collection will be validated. 

When importing into the corresponding collection, these base properties will always be imported:
* for document collections, `_key` - equivalent to SQL primary key
* for edge collections and generated edge collections, `_from` and `_to`, equivalent to foreign keys of many-to-many SQL tables
Thus, all many-to-many tables in the SQL schema should be imported as edge collections, while other tables as document collections. JSONschema validation does not work for these base properties, so they should not be specified inside the JSONschema object.

Other than these, only the specified properties (SQL columns) of a document will be imported. ArangoDB data types of document fields will be predetermined based on their corresponding SQL column data type.

Edge collection objects contain three fields: `schema` contains the aforementioned ArangoDB schema specification object, and `from` and `to` describe the collections with the given relationship.

Generated edge collections, meant to represent one-to-many relationships, will be automatically generated after all other collections and edge collections are imported. They contain three fields: `attributeCollection` represents the child collection whose attributes will be used to generate edges, and `from` and `to` represent objects containing two fields: `collection` and `attribute`, describing collections with the given relationship, and respectively the attribute inside `attributeCollection` which represents their SQL primary key / foreign key.

It is highly recommended not to remove any of the predefined collections or attributes, as this may affect functionality of searches! (They were designed with the predefined schema in mind). However, any other collections or attributes present in the original SQL schema can be imported.

To avoid idle timeouts resulting from excessively large transactions, the import can be paginated, so that certain large queries will be split into smaller ones, leading to increased stability.

### How to import
To import the database into ArangoDB, use the following endpoint:
* `codex/import/import`: POST - parameters `boolean complete` (whether to execute second phase of import, or only the first), `integer pagecount` (number of small subqueries to split large queries into - minimum 10 recommended) - returns the string `Import complete` on a success

A partial import (at only the first stage) can also be led into the second using the endpoint:
* `codex/import/optimize`: POST - parameter `integer pagecount` - returns the string `Import complete` on a success

## Search Endpoints
Endpoints for searches have a number of similar fields: `wordform` represents the form to search against (either `accent` for accented forms, or `noaccent` for forms without accent), `relationtype` represents a relationship between two words (`synonym`, `antonym`, `diminutive` or `augmentative`)
### Endpoints usable for any stage of import
* `codex/search/levenshtein`: POST - parameters `String word`, `Integer distance`, `String wordform` - returns array of words with a Levenshtein distance of maximum `dist` from `word`, using the specified `wordform`
* `codex/search/regex`: POST - parameter `String regex`, `String wordform` - returns array of strings representing words matching the Regex expression `regex` (as described [here](https://www.arangodb.com/docs/stable/aql/functions-string.html#regular-expression-syntax)) using specified `wordform`
* `codex/knn/editdistance`: POST - parameters `String word`, `String wordform`, `String distancetype` (either `levenshtein`, `hamming` or `lcs_distance`), `Integer neighborcount` - returns array of strings representing K nearest neighbors using given `distancetype`
* `codex/knn/ngram`: POST - parameters `String word`, `String wordform`, `String distancetype` (either `ngram_similarity` (described [here](https://www.arangodb.com/docs/stable/aql/functions-string.html#ngram_similarity)) or `ngram_positional_similarity` (described [here](https://www.arangodb.com/docs/stable/aql/functions-string.html#ngram_positional_similarity))), `String neighborcount`, `Integer ngramsize` - returns array of strings representing K nearest neighbors using given N-gram `distancetype` and given `ngramsize`
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

### Performance tests
The folder `tests` contains a number of Jmeter performance / load tests. 
Number of threads, rampup time and number of loops can be specified through the command line parameters `-Jthreads`, `-Jrampup` and `-Jloops` respectively (default value is 1 for each).
Ramp-up time represents the amount of time in seconds necessary for all testing threads to start: for example, for 5 threads and a ramp-up time of 10, a request will be sent every 10/5 = 2 seconds. This sequence will be executed an amount of times equal to the number of loops.

To start one of the tests, run the following command:

`sudo docker run -v {absolute path to 'tests' folder}:/workspace --net=host swethapn14/repo_perf:JmeterLatest -Jthreads={x} -Jrampup={x} -Jloops={x} -n -t /workspace/{testname}/{testname}.jmx -l /workspace/logs/{testname}.jtl -f -e -o /workspace/html/{testname}`

where `testname` is the name of the test's directory.

This will store an HTML summary of the test results in `tests/html/{testname}`, and a log file in `tests/logs/{testname}`.

### Known issues/limitations
* `"java.io.IOException: Reached the end of the stream"` error: caused by an exceedingly large transaction surpassing [ArangoDB's stream transaction idle timeout](https://www.arangodb.com/docs/stable/transactions-stream-transactions.html). The default timeout is 60 seconds, and this is mitigated somewhat by having the server option `--transaction.streaming-idle-timeout` set to the maximum possible value of 120 seconds in the database's Dockerfile. Nevertheless, ArangoDB is not built with large transactions in mind, so it is recommended to split any large transactions into smaller ones, such as by increasing the `pagecount` when importing.
* Searches for diminutives or augmentatives are not heavily supported; very few of these relationships exist in the original SQL database. The main focus for relation searches is on synonyms, and to a lesser extent antonyms.
* Most lexemes in common use contain meanings extracted from their definitions, for easier presentation in a tree format; some in lesser use do not have meanings extracted separately, but they do have definitions, presented in the website and stored in the SQL table `Definition`
* For some lesser used lexemes, DEXonline redirects to content of a more used version (for example Rosa -> trandafir), whose usage examples and etymologies may not always contain/describe the same word, but another form or synonym of it
## TODO:
* performance tests
