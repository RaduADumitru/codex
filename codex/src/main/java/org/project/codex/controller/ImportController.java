package org.project.codex.controller;

import com.fasterxml.jackson.databind.JsonNode;
import net.minidev.json.JSONArray;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.project.codex.enums.ArangoDataType;
import org.project.codex.enums.JsonSchemaDataType;
import org.project.codex.exceptions.ImportException;
import org.project.codex.forms.OptimizeImportForm;
import org.project.codex.repository.QueryRepository;
import org.project.codex.util.ColumnData;
import org.project.codex.util.EdgeDefinition;
import org.project.codex.util.ImportUtil;
import org.project.codex.enums.SqlDataType;
import org.project.codex.forms.ImportForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;


@PropertySource(value = "classpath:application.properties")
@PropertySource(value = "classpath:arangodb.properties")
@RestController
@RequestMapping("/codex/import/")
public class ImportController {

    private final QueryRepository repository;

    //Get configuration
    @Value("${arangodb.host}")
    private String arangoDbHost;
    @Value("${arangodb.port}")
    private Integer arangoDbPort;
    @Value("${arangodb.user}")
    private String arangoDbUser;
    @Value("${arangodb.password}")
    private String arangoDbPassword;
    private AtomicBoolean importing = new AtomicBoolean(false);
    @Value("${import.link}")
    private String downloadLink;
    public ImportController(QueryRepository repository) {
        this.repository = repository;
    }

    //Test sending request to DB API
    @GetMapping("version")
    ResponseEntity<String> getVersion() throws IOException {
        String credential = ImportUtil.getInstance().getCredentials();
        Request request = new Request.Builder()
                .url("http://localhost:8529/_api/version")
                .addHeader("Content-Type", "application/json")
                .header("Authorization", credential)
                .build();
        Call call = ImportUtil.getInstance().getOkHttpClient().newCall(request);
        Response response = call.execute();
        String responseText = Objects.requireNonNull(response.body()).string();
        response.close();
        return new ResponseEntity<>(Objects.requireNonNull(responseText), HttpStatus.OK);
    }

    @PostMapping("import")
    ResponseEntity<String> databaseImport(@org.springframework.web.bind.annotation.RequestBody ImportForm importForm) throws IOException, JSQLParserException, ImportException {
        boolean completeImport = importForm.isComplete();
        Integer pageCount = importForm.getPagecount();
        if(pageCount < 0) {
            throw new IllegalArgumentException("Page count must be a positive integer!");
        }
        if(importing.get()) {
            throw new ImportException("Import already in progress!");
        }
        else {
            importing.compareAndSet(false, true);
            System.out.println("Starting import...");
            try {
                String baseRequestUrl = "http://" + arangoDbHost + ":" + arangoDbPort + "/_db/dex/_api/";
                ImportUtil.getInstance().setBaseRequestUrl(baseRequestUrl);
                ImportUtil.getInstance().setCredentials(Credentials.basic(arangoDbUser, arangoDbPassword));

                ImportUtil.deleteGraph("LexemeRelationGraph");

                //open import schema file and store it in JsonNode for traversal

                String importSchemaFileName = "import-schema.json";
                Resource importSchemaFile = new ClassPathResource(importSchemaFileName);
                InputStream importSchemaInputStream = importSchemaFile.getInputStream();
                JsonNode importSchema = ImportUtil.getInstance().getObjectMapper().readTree(importSchemaInputStream);
                importSchemaInputStream.close();

                String finalSchemaFileName = "final-schema.json";
                Resource finalSchemaFile = new ClassPathResource(finalSchemaFileName);
                InputStream finalSchemaInputStream = finalSchemaFile.getInputStream();
                JsonNode finalSchema = ImportUtil.getInstance().getObjectMapper().readTree(finalSchemaInputStream);
                finalSchemaInputStream.close();

                //store schema colections, only SQL related to these will be parsed
                Iterator<String> importSchemaCollectionIterator;
                if (importSchema.has("collections")) {
                    importSchemaCollectionIterator = importSchema.get("collections").fieldNames();
                } else {
                    throw new ImportException("Schema document " + importSchemaFileName + " is missing field 'collections'!");
                }
                Iterator<String> importSchemaEdgeCollectionIterator = importSchema.get("edgeCollections").fieldNames();
                if (importSchema.has("edgeCollections")) {
                    importSchemaEdgeCollectionIterator = importSchema.get("edgeCollections").fieldNames();
                } else {
                    throw new ImportException("Schema document " + importSchemaFileName + " is missing field `edgeCollections`!");
                }
                Iterator<String> importSchemaGeneratedEdgeCollectionIterator;
                if (importSchema.has("generatedEdgeCollections")) {
                    importSchemaGeneratedEdgeCollectionIterator = importSchema.get("generatedEdgeCollections").fieldNames();
                } else {
                    throw new ImportException("Schema document " + importSchemaFileName + " is missing field `generatedEdgeCollections`!");
                }
                Iterator<String> finalSchemaCollectionIterator;
                if (finalSchema.has("collections")) {
                    finalSchemaCollectionIterator = finalSchema.get("collections").fieldNames();
                } else {
                    throw new ImportException("Final schema document " + finalSchemaFileName + " is missing field 'collections'!");
                }
                Iterator<String> finalSchemaEdgeCollectionIterator;
                if (finalSchema.has("edgeCollections")) {
                    finalSchemaEdgeCollectionIterator = finalSchema.get("edgeCollections").fieldNames();
                } else {
                    throw new ImportException("Final schema document " + finalSchemaFileName + " is missing field 'edgeCollections'!");
                }
                HashSet<String> importSchemaCollections = new HashSet<>();
                importSchemaCollectionIterator.forEachRemaining(importSchemaCollections::add);

                HashSet<String> importSchemaEdgeCollections = new HashSet<>();
                importSchemaEdgeCollectionIterator.forEachRemaining(importSchemaEdgeCollections::add);

                HashSet<String> importSchemaGeneratedEdgeCollections = new HashSet<>();
                importSchemaGeneratedEdgeCollectionIterator.forEachRemaining(importSchemaGeneratedEdgeCollections::add);

                HashSet<String> finalSchemaCollections = new HashSet<>();
                finalSchemaCollectionIterator.forEachRemaining(finalSchemaCollections::add);

                HashSet<String> finalSchemaEdgeCollections = new HashSet<>();
                finalSchemaEdgeCollectionIterator.forEachRemaining(finalSchemaEdgeCollections::add);


                //for each collection, store columns in schema along with their data types
                HashMap<String, HashMap<String, ColumnData>> sqlColumnPositionMap = new HashMap<>();
                HashMap<String, TreeSet<ColumnData>> colDataMap = new HashMap<>();

                //validate schema for generated edge collections
                for(String generatedCollection : importSchemaGeneratedEdgeCollections) {
                    if(importSchemaCollections.contains(generatedCollection)) {
                        throw new ImportException("Import schema: generated edge collection " + generatedCollection + " also exists as document collection!");
                    }
                    if(importSchemaEdgeCollections.contains(generatedCollection)) {
                        throw new ImportException("Import schema: generated edge collection " + generatedCollection + " also exists as edge collection!");
                    }
                    for(String mandatoryField : new String[]{"attributeCollection", "from", "to"}) {
                        if(!importSchema.get("generatedEdgeCollections").get(generatedCollection).has(mandatoryField)) {
                            throw new ImportException("Import schema: generated edge collection " + generatedCollection + " missing attribute '" + mandatoryField + "'!");
                        }
                    }
                    for(String directionName : new String[]{"from", "to"}) {
                        for(String directionField : new String[]{"collection", "attribute"}) {
                            if(!importSchema.get("generatedEdgeCollections").get(generatedCollection).get(directionName).has(directionField)) {
                                throw new ImportException("Import schema: generated edge collection " + generatedCollection + " missing attribute '" + directionField + "' in field '" + directionName + "'!");
                            }
                        }
                    }
                }

                HashSet<String> validJSONschemaTypes = new HashSet<>(List.of("integer", "string", "number", "boolean", "null"));
                //Check if any properties are missing type
                for(String collection : importSchemaCollections) {
                    for (Iterator<String> it = importSchema.get("collections").get(collection).get("rule").get("properties").fieldNames(); it.hasNext(); ) {
                        String property = it.next();
                        if(!importSchema.get("collections").get(collection).get("rule").get("properties").get(property).has("type")) {
                            throw new ImportException("Import schema, collection " + collection + ", attribute " + property + ": missing type!");
                        }
                        if(!validJSONschemaTypes.contains(importSchema.get("collections").get(collection).get("rule").get("properties").get(property).get("type").textValue())) {
                            throw new ImportException("Import schema, collection " + collection + ", attribute " + property + " has an invalid type! (needs to be one of " + validJSONschemaTypes);
                        }
                    }
                }
                for(String collection : importSchemaEdgeCollections) {
                    for (Iterator<String> it = importSchema.get("edgeCollections").get(collection).get("schema").get("rule").get("properties").fieldNames(); it.hasNext(); ) {
                        String property = it.next();
                        if(!importSchema.get("edgeCollections").get(collection).get("schema").get("rule").get("properties").get(property).has("type")) {
                            throw new ImportException("Import schema, collection " + collection + ", attribute " + property + ": missing type!");
                        }
                        if(!validJSONschemaTypes.contains(importSchema.get("edgeCollections").get(collection).get("schema").get("rule").get("properties").get(property).get("type").textValue())) {
                            throw new ImportException("Import schema, edge collection " + collection + ", attribute " + property + " has an invalid type! (needs to be one of " + validJSONschemaTypes);
                        }
                    }
                }
                String currentCollectionName = null;
                StringBuilder insertRequestBody = new StringBuilder();
                String insertRequestBodyHeader = null;

                //load archived database SQL script from remote link
                URL url = new URL(downloadLink);

                //create statements will be built starting with lines beginning with "CREATE" and ending with ";"
                boolean buildingCreateStatement = false;
                StringBuilder createStatement = new StringBuilder();

                boolean isDocumentCollection = false;
                boolean isEdgeCollection = false;

                //Delete all collections, so import is fresh
                ImportUtil.deleteCollections(repository.getCollections());

                try(InputStream stream = new GZIPInputStream(url.openStream()); BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                    // traverse each line of script;
                    // starting from lines containing keyword CREATE, Create statements will be built until reaching ;, then parsed and executed according to schema
                    //lines containing INSERT will be treated as insert statements, and parsed if collection is in schema: attributes in schema will be inserted
                    String line;
                    while((line = reader.readLine()) != null)
                        if (buildingCreateStatement) {
                            //check if line reached is not end of create statement
                            if (line.indexOf(';') == -1) {
                                createStatement.append(line);
                            } else {
                                createStatement.append(");");
                                //parse finished statement and send request
                                CreateTable createTable = (CreateTable) CCJSqlParserUtil.parse(String.valueOf(createStatement));
                                //Get collection name: will be surrounded by apostrophes, which need to be removed
                                currentCollectionName = createTable.getTable().getName().replace("`", "");
                                //if collection is in schema, create it as normal collection
                                isDocumentCollection = importSchemaCollections.contains(currentCollectionName);
                                isEdgeCollection = importSchemaEdgeCollections.contains(currentCollectionName);
                                if (isDocumentCollection && isEdgeCollection) {
                                    throw new ImportException("Collection " + currentCollectionName + " is both document collection and edge collection in import schema!");
                                }
                                if (isDocumentCollection || isEdgeCollection) {
                                    String arangoSchema;
                                    if(isDocumentCollection) {
                                        arangoSchema = importSchema.get("collections").get(currentCollectionName).toString();
                                    }
                                    else {
                                        arangoSchema = importSchema.get("edgeCollections").get(currentCollectionName).get("schema").toString();
                                    }
                                    //Send create collection request
                                    ImportUtil.createCollection(currentCollectionName, isEdgeCollection, arangoSchema);
                                    // Memorise indexes of each column in collection
                                    sqlColumnPositionMap.put(currentCollectionName, new HashMap<>());
                                    List<ColumnDefinition> columnDefinitions = createTable.getColumnDefinitions();
                                    if(currentCollectionName.equals("ObjectTag")) {
                                        //column 0 id, column 1 objectId, column 2 objectType, column 3 tagId: swap columns 2 and 3 to respect same structure as other edge collections
                                        Collections.swap(columnDefinitions, 2, 3);
                                    }
                                    //Index starts from 1: skip id
                                    Integer columnIndex;
                                    // Stores column definitions for all columns except defaults (for document collections: id (column 0), for edge collections: id (col 0) and from/to id (columns 1 and 2)
                                    List<ColumnDefinition> collectionColumnDefinitions;
                                    if (isDocumentCollection) {
                                        columnIndex = 1;
                                        collectionColumnDefinitions = columnDefinitions.subList(1, columnDefinitions.size());
                                    } else {
                                        columnIndex = 3;
                                        collectionColumnDefinitions = columnDefinitions.subList(3, columnDefinitions.size());
                                    }
                                    //Store position of each column in map; first will not be stored since it always represents id
                                    for (ColumnDefinition columnDefinition : collectionColumnDefinitions) {
                                        String columnName = columnDefinition.getColumnName().replace("`", "");
                                        String columnDataType = columnDefinition.getColDataType().getDataType();
                                        sqlColumnPositionMap.get(currentCollectionName).put(columnName, new ColumnData(columnName, columnIndex, SqlDataType.valueOf(columnDataType.toUpperCase()).getArangoDataType()));
                                        columnIndex++;
                                    }

                                    //add data types of columns in schema for this collection

                                    colDataMap.put(currentCollectionName, new TreeSet<>());
                                    Iterator<String> fieldsIterator = null;
                                    if (isDocumentCollection) {
                                        if (!importSchema.get("collections").get(currentCollectionName).has("rule")) {
                                            throw new ImportException("Import schema document collection " + currentCollectionName + " lacks field 'rule'!");
                                        }
                                        if (!importSchema.get("collections").get(currentCollectionName).get("rule").has("properties")) {
                                            throw new ImportException("Import schema document collection " + currentCollectionName + " lacks field 'properties'!");
                                        }
                                        fieldsIterator = importSchema.get("collections").get(currentCollectionName).get("rule").get("properties").fieldNames();
                                    } else {
                                        if (!importSchema.get("edgeCollections").get(currentCollectionName).has("schema")) {
                                            throw new ImportException("Import schema edge collection " + currentCollectionName + " lacks field 'schema'!");
                                        }
                                        if (!importSchema.get("edgeCollections").get(currentCollectionName).get("schema").has("rule")) {
                                            throw new ImportException("Import schema edge collection " + currentCollectionName + " lacks field 'schema.rule'!");
                                        }
                                        if (!importSchema.get("edgeCollections").get(currentCollectionName).get("schema").get("rule").has("properties")) {
                                            throw new ImportException("Import schema edge collection " + currentCollectionName + " lacks field 'schema.rule.properties'!");
                                        }
                                        fieldsIterator = importSchema.get("edgeCollections").get(currentCollectionName).get("schema").get("rule").get("properties").fieldNames();
                                    }
                                    while (fieldsIterator != null && fieldsIterator.hasNext()) {
                                        String fieldName = fieldsIterator.next();
                                        if (sqlColumnPositionMap.get(currentCollectionName).containsKey(fieldName)) {
                                            Integer columnPosition = sqlColumnPositionMap.get(currentCollectionName).get(fieldName).getPosition();
                                            ArangoDataType columnDataType = sqlColumnPositionMap.get(currentCollectionName).get(fieldName).getDataType();
                                            colDataMap.get(currentCollectionName).add(new ColumnData(fieldName, columnPosition, columnDataType));
                                        } else {
                                            throw new ImportException("Column " + fieldName + " in collection " + currentCollectionName + " in import schema doesn't exist in database!");
                                        }
                                    }
                                    //Start building insert request body: first row represents column names inserted
                                    ArrayList<String> insertColumns = new ArrayList<>();
                                    //_key will always be imported first, or _from and _to in case of edge collections
                                    if (isDocumentCollection) {
                                        insertColumns.add("_key");
                                    } else if (isEdgeCollection) {
                                        insertColumns.add("_from");
                                        insertColumns.add("_to");
                                    }
                                    for (ColumnData colData : colDataMap.get(currentCollectionName)) {
                                        insertColumns.add(colData.getColumnName());
                                    }
                                    insertRequestBodyHeader = JSONArray.toJSONString(insertColumns);
                                }
                                //reset built statement
                                createStatement = new StringBuilder();
                                buildingCreateStatement = false;
                            }
                        }
//            //case when create statement is not being built
                        else {
                            //parse if in collection, and send request
                            if (line.startsWith("INSERT")) {
                                if (isDocumentCollection || isEdgeCollection) {
                                    //Import script is MySQL script and uses \ escape character
                                    //JSQLparser treats both \ and ' as escape characters, and bugs out when one escapes the other; they need to be separated, and \' replaced with ''
                                    line = line
                                            .replaceAll("([^\\\\])((\\\\\\\\)+)([\\\\'])", "$1$2 $4") //Separates non escaping backslashes from apostrophes with a space
                                            .replace("\\'", "''") //Transforms backslash escaped apostrophes into double apostrophe escape
                                            .replace("'\\", "' \\") //MySQL doesn't use commas as escape character, separate so that they are not interpreted as escape
                                    ;
                                    Insert insert = (Insert) CCJSqlParserUtil.parse(line);
                                    if(insert == null) {
                                        throw new ImportException("Line: " + line + "\nCould not parse insert! (Possibly due to unsupported character inside)");
                                    }
                                    ItemsList itemsList = insert.getItemsList();

                                    insertRequestBody = new StringBuilder();
                                    insertRequestBody.append(insertRequestBodyHeader);

                                    String fromCollection = null;
                                    String toCollection = null;
                                    if (isEdgeCollection && !currentCollectionName.equals("ObjectTag")) {
                                        if (importSchema.get("edgeCollections").get(currentCollectionName).has("from")) {
                                            fromCollection = importSchema.get("edgeCollections").get(currentCollectionName).get("from").asText();
                                            if (!importSchemaCollections.contains(fromCollection)) {
                                                throw new ImportException("Import schema: 'from' collection " + fromCollection + " of edge collection " + currentCollectionName + " is not document collection!");
                                            }
                                        } else {
                                            throw new ImportException("Import schema: edge collection " + currentCollectionName + " missing 'from' attribute!");
                                        }
                                        if (importSchema.get("edgeCollections").get(currentCollectionName).has("to")) {
                                            toCollection = importSchema.get("edgeCollections").get(currentCollectionName).get("to").asText();
                                            if (!importSchemaCollections.contains(toCollection)) {
                                                throw new ImportException("Import schema: 'to' collection " + fromCollection + " of edge collection " + currentCollectionName + " is not document collection!");
                                            }
                                        } else {
                                            throw new ImportException("Import schema: edge collection " + currentCollectionName + " missing 'to' attribute!");
                                        }
                                    }


                                    if (itemsList instanceof ExpressionList) {
                                        List<Expression> rowList = ((ExpressionList) itemsList).getExpressions();

                                        //Store JSONschema types of each property
                                        HashMap<String, JsonSchemaDataType> JSONschemaAttributeType = new HashMap<>();
                                        for (ColumnData colData : colDataMap.get(currentCollectionName)) {
                                            if(isDocumentCollection) {
                                                JSONschemaAttributeType.put(colData.getColumnName(), JsonSchemaDataType.valueOf(importSchema.get("collections").get(currentCollectionName).get("rule").get("properties").get(colData.getColumnName()).get("type").textValue().toUpperCase()));
                                            }
                                            else {
                                                JSONschemaAttributeType.put(colData.getColumnName(), JsonSchemaDataType.valueOf(importSchema.get("edgeCollections").get(currentCollectionName).get("schema").get("rule").get("properties").get(colData.getColumnName()).get("type").textValue().toUpperCase()));
                                            }
                                        }
                                        for (Expression row : rowList) {
                                            //where values for a row will be stored
                                            List expressionValues = new ArrayList();
                                            //store expressions containing values which will be added
                                            RowConstructor rowConstructor = (RowConstructor) row;
                                            List<Expression> rowExpressionList = rowConstructor.getExprList().getExpressions();
                                            if (isDocumentCollection) {
                                                Expression keyExpression = rowExpressionList.get(0);
                                                if (keyExpression instanceof LongValue) {
                                                    expressionValues.add(String.valueOf(((LongValue) keyExpression).getValue()));
                                                } else {
                                                    throw new ImportException("Insert into collection " + currentCollectionName + ": key expression " + keyExpression + " not LongValue!");
                                                }
                                                //if document collection, always store id, which will be first element
                                            } else {
                                                if(currentCollectionName.equals("ObjectTag")) {
                                                    //swap values for object type and tag id, to match swap of column definitions
                                                    Collections.swap(rowExpressionList, 2, 3);
                                                }
                                                // add _from and _to values, which will be second and third expressions
                                                Expression fromExpression = rowExpressionList.get(1);
                                                Expression toExpression = rowExpressionList.get(2);
                                                if(currentCollectionName.equals("ObjectTag")) {
                                                    //ObjectTag collection describes relations between multiple pairs of collections, according to collection type
                                                    Expression objectTypeExpression = rowExpressionList.get(3);
                                                    if(objectTypeExpression instanceof LongValue) {
                                                        long objectTypeValue = ((LongValue)objectTypeExpression).getValue();
                                                        /*
                                                          1 - definition
                                                          2 - lexeme
                                                          3 - meaning
                                                          4 - source
                                                          5 - definition version
                                                          6 - entry
                                                          7 - tree
  */
                                                        if(objectTypeValue == 1) {
                                                            fromCollection = "Definition";
                                                        }
                                                        else if(objectTypeValue == 2) {
                                                            fromCollection = "Lexeme";
                                                        }
                                                        else if(objectTypeValue == 3) {
                                                            fromCollection = "Meaning";
                                                        }
                                                        else if(objectTypeValue == 4) {
                                                            fromCollection = "Source";
                                                        }
                                                        else if(objectTypeValue == 5) {
                                                            fromCollection = "DefinitionVersion";
                                                        }
                                                        else if(objectTypeValue == 6) {
                                                            fromCollection = "Entry";
                                                        }
                                                        else if(objectTypeValue == 7) {
                                                            fromCollection = "Tree";
                                                        }
                                                        else {
                                                            throw new ImportException("Error importing ObjectTag: type " + objectTypeValue + " not defined!");
                                                        }
                                                        toCollection = "Tag";
                                                    }
                                                }
                                                // add _from value
                                                if (fromExpression instanceof LongValue) {
                                                    // _from pattern : from collection name + '/' + id
                                                    expressionValues.add(fromCollection + "/" + ((LongValue) fromExpression).getValue());
                                                } else {
                                                    throw new ImportException("Insert into edge collection " + currentCollectionName + ": from expression " + fromExpression + " not LongValue!");
                                                }

                                                // add _to value
                                                if (toExpression instanceof LongValue) {
                                                    // to pattern : to collection name + '/' + id
                                                    expressionValues.add(toCollection + "/" + ((LongValue) toExpression).getValue());
                                                } else {
                                                    throw new ImportException("Insert into edge collection " + currentCollectionName + ": to expression " + fromExpression + " not LongValue!");
                                                }
                                            }
                                            //add proper columns to added row expressions
                                            //traverse data of all columns in schema corresponding to collection
                                            for (ColumnData colData : colDataMap.get(currentCollectionName)) {
                                                Expression valueExpression = rowExpressionList.get(colData.getPosition());


                                                JsonSchemaDataType schemaDataType = JSONschemaAttributeType.get(colData.getColumnName());
                                                // Add values: for each type of SQL parsed value, treat according to schema datatype
                                                if (schemaDataType.equals(JsonSchemaDataType.NULL)) {
                                                    expressionValues.add(null);
                                                }
                                                else {
                                                    if (valueExpression instanceof StringValue) {
                                                        String value = ((StringValue) valueExpression).getValue()
                                                                .replace("''", "'")
                                                                .trim()
                                                                .replace("\\ '", "\\'");
                                                        if (schemaDataType.equals(JsonSchemaDataType.STRING)) {
                                                            //ArangoDB doesn't escape double quotes added for SQL parsing; replace them manually
                                                            expressionValues.add(value);
                                                        } else if (schemaDataType.equals(JsonSchemaDataType.BOOLEAN)) {
                                                            try {
                                                                if(value.equals("0") || value.equals("false")) {
                                                                    expressionValues.add(false);
                                                                }
                                                                else if(value.equals("1") || value.equals("true")) {
                                                                    expressionValues.add(true);
                                                                }
                                                                else {
                                                                    throw new IllegalArgumentException();
                                                                }
                                                            }
                                                            catch(Exception e){
                                                                throw new ImportException("Import schema type mismatch: collection " + currentCollectionName + ", attribute " + colData.getColumnName() + ": " + "couldn't convert string value " + value + " to " + schemaDataType);
                                                            }
                                                        }
                                                        else if(schemaDataType.equals(JsonSchemaDataType.INTEGER)) {
                                                            try {
                                                                expressionValues.add(Integer.valueOf(value));
                                                            }
                                                            catch(Exception e){
                                                                throw new ImportException("Import schema type mismatch: collection " + currentCollectionName + ", attribute " + colData.getColumnName() + ": " + " couldn't convert string value " + value + " to " + schemaDataType);
                                                            }
                                                        }
                                                        else if(schemaDataType.equals(JsonSchemaDataType.NUMBER)) {
                                                            try {
                                                                expressionValues.add(Double.valueOf(value));
                                                            }
                                                            catch(Exception e){
                                                                throw new ImportException("Import schema type mismatch: collection " + currentCollectionName + ", attribute " + colData.getColumnName() + ": " + " couldn't convert string value " + value + " to " + schemaDataType);
                                                            }
                                                        }
                                                        else {
                                                            throw new ImportException("Import schema type mismatch: collection " + currentCollectionName + " attribute " + colData.getColumnName() + ": schema data type could not be determined!");
                                                        }
                                                    }
                                                    else
                                                    if (valueExpression instanceof DoubleValue) {
                                                        Double value = ((DoubleValue) valueExpression).getValue();
                                                        if (schemaDataType.equals(JsonSchemaDataType.NUMBER)) {
                                                            expressionValues.add(value);
                                                        }
                                                        else if(schemaDataType.equals(JsonSchemaDataType.STRING)) {
                                                            expressionValues.add(String.valueOf(value));
                                                        }
                                                        else if(schemaDataType.equals(JsonSchemaDataType.INTEGER)) {
                                                            try {
                                                                expressionValues.add(Integer.valueOf(String.valueOf(value)));
                                                            }
                                                            catch(Exception e){
                                                                throw new ImportException("Import schema type mismatch: collection " + currentCollectionName + ", attribute " + colData.getColumnName() + ": " + " couldn't convert double value " + value + " to " + schemaDataType);
                                                            }
                                                        }
                                                        else if(schemaDataType.equals(JsonSchemaDataType.BOOLEAN)) {
                                                            try {
                                                                if(value == 0) {
                                                                    expressionValues.add(false);
                                                                }
                                                                else if(value == 1) {
                                                                    expressionValues.add(true);
                                                                }
                                                                else {
                                                                    throw new IllegalArgumentException();
                                                                }
                                                            }
                                                            catch(Exception e){
                                                                throw new ImportException("Import schema type mismatch:  collection " + currentCollectionName + ", attribute " + colData.getColumnName() + ": " + "couldn't convert double value " + value + " to " + schemaDataType);
                                                            }
                                                        }
                                                        else {
                                                            throw new ImportException("Import schema type mismatch: collection " + currentCollectionName + " attribute " + colData.getColumnName() + ": schema data type could not be determined!");
                                                        }
                                                    }
                                                    else if (valueExpression instanceof LongValue) {
                                                        Long value = ((LongValue)valueExpression).getValue();
                                                        if (schemaDataType.equals(JsonSchemaDataType.INTEGER)) {
                                                            expressionValues.add(value);
                                                        }
                                                        else if (schemaDataType.equals(JsonSchemaDataType.NUMBER)) {
                                                            expressionValues.add(Double.valueOf(value));
                                                        }
                                                        else if(schemaDataType.equals(JsonSchemaDataType.STRING)) {
                                                            expressionValues.add(String.valueOf(value));
                                                        }
                                                        else if (schemaDataType.equals(JsonSchemaDataType.BOOLEAN)) {
                                                            try {
                                                                if(value == 0) {
                                                                    expressionValues.add(false);
                                                                }
                                                                else if(value == 1) {
                                                                    expressionValues.add(true);
                                                                }
                                                                else {
                                                                    throw new IllegalArgumentException();
                                                                }
                                                            }
                                                            catch(Exception e){
                                                                throw new ImportException("Import schema type mismatch: collection " + currentCollectionName + ", attribute " + colData.getColumnName() + ": " + " couldn't convert long value " + value + " to " + schemaDataType);
                                                            }
                                                        }
                                                        else {
                                                            throw new ImportException("Import schema type mismatch: collection " + currentCollectionName + " attribute " + colData.getColumnName() + ": schema data type could not be determined!");
                                                        }
                                                    }
                                                    else if (valueExpression instanceof NullValue) {
                                                        expressionValues.add(null);
                                                    }
                                                    else if (valueExpression instanceof DateValue) {
                                                        Date value = ((DateValue) valueExpression).getValue();
                                                        if (schemaDataType.equals(JsonSchemaDataType.STRING)) {
                                                            expressionValues.add(value.toString());
                                                        } else {
                                                            throw new ImportException("Import schema type mismatch:  collection " + currentCollectionName + ", attribute " + colData.getColumnName() + ": " + "couldn't convert date value " + value + " to " + schemaDataType);
                                                        }
                                                    }
                                                    else if (valueExpression instanceof TimeValue) {
                                                        Time value = ((TimeValue) valueExpression).getValue();
                                                        if (schemaDataType.equals(JsonSchemaDataType.STRING)) {
                                                            expressionValues.add(value.toString());
                                                        } else {
                                                            throw new ImportException("Import schema type mismatch: collection " + currentCollectionName + ", attribute " + colData.getColumnName() + ": " + " couldn't convert time value " + value + " to " + schemaDataType);
                                                        }
                                                    }
                                                    else if (valueExpression instanceof TimestampValue) {
                                                        Timestamp value = ((TimestampValue) valueExpression).getValue();
                                                        if (colData.getDataType().equals(ArangoDataType.STRING)) {
                                                            expressionValues.add(value.toString());
                                                        } else {
                                                            throw new ImportException("Import schema type mismatch: collection " + currentCollectionName + ", attribute " + colData.getColumnName() + ": " + " couldn't convert timestamp value " + value + " to " + schemaDataType);
                                                        }
                                                    }
                                                    else {
                                                        throw new ImportException("SQL parsing error: type for expression + " + valueExpression + " in collection " + currentCollectionName + "could not be determined!");
                                                    }
                                                }

                                            }
                                            insertRequestBody.append('\n').append(JSONArray.toJSONString(expressionValues));
                                        }
                                    } else {
                                        throw new ImportException("Items List " + itemsList + " not ExpressionList!");
                                    }
                                    String jsonString = insertRequestBody.toString();
                                    RequestBody insertFormBody = FormBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
                                    String insertRequestUrl = ImportUtil.getInstance().getBaseRequestUrl() + "import?collection=" + currentCollectionName + "&complete=false&details=true";
                                    Request insertRequest = new Request.Builder()
                                            .url(insertRequestUrl)
                                            .addHeader("Content-type", "application/json")
                                            .addHeader("Accept", "application/json")
                                            .header("Authorization", ImportUtil.getInstance().getCredentials())
                                            .post(insertFormBody)
                                            .build();
                                    Call insertCall = ImportUtil.getInstance().getOkHttpClient().newCall(insertRequest);
                                    Response insertResponse = insertCall.execute();
                                    String insertResponseBody = Objects.requireNonNull(insertResponse.body()).string();
                                    JsonNode insertResponseNode = ImportUtil.getInstance().getObjectMapper().readTree(insertResponseBody);
                                    if(insertResponseNode.get("errors").asLong() != 0) {
                                        //throw exception describing documents which didn't pass schema validation
                                        throw new ImportException("Insert into collection " + currentCollectionName + ": following documents failed validation according to import schema: " + insertResponseNode.get("details").toPrettyString().replace("\"at position", "\n\"at position"));
                                    }
                                    else {
                                        System.out.println("Inserted documents into " + currentCollectionName + ": " + insertResponseBody);
                                    }
                                    insertResponse.close();
                                }
                            }
                            else {
                                if (line.startsWith("CREATE")) {
                                    String createCollection = StringUtils.substringBetween(line, "CREATE TABLE `", "`");
                                    System.out.println("Parsing SQL for table: " + createCollection);
                                    if (importSchemaCollections.contains(createCollection) || importSchemaEdgeCollections.contains(createCollection)) {
                                        if (importSchemaCollections.contains(createCollection)) {
                                            System.out.println("Found document collection " + createCollection + " in import schema");
                                        } else if (importSchemaEdgeCollections.contains(createCollection)) {
                                            System.out.println("Found edge collection " + createCollection + " in import schema");
                                        }
                                        //begin building create statement to parse afterwards
                                        buildingCreateStatement = true;
                                        createStatement.append(line);
                                    }
                                    else {
                                        isDocumentCollection = false;
                                        isEdgeCollection = false;
                                    }
                                }
                            }
                        }
                    }
                System.out.println("Initial data import complete!");
                System.out.println("Creating generated collections:");

                //Create generated collections

                //store collection document counts here, to avoid repeated queries
                HashMap<String, Integer> documentCountCache = new HashMap<>();

                for(String generatedCollection : importSchemaGeneratedEdgeCollections) {
                    System.out.println("Generating collection " + generatedCollection);
                    String attributeCollection = importSchema.get("generatedEdgeCollections").get(generatedCollection).get("attributeCollection").asText();

                    String fromCollection = importSchema.get("generatedEdgeCollections").get(generatedCollection).get("from").get("collection").asText();
                    String fromAttribute = importSchema.get("generatedEdgeCollections").get(generatedCollection).get("from").get("attribute").asText();

                    String toCollection = importSchema.get("generatedEdgeCollections").get(generatedCollection).get("to").get("collection").asText();
                    String toAttribute = importSchema.get("generatedEdgeCollections").get(generatedCollection).get("to").get("attribute").asText();

                    ImportUtil.createCollection(generatedCollection, true, null);

                    if(pageCount == 0) {
                        repository.insertIntoGeneratedCollection(fromCollection, toCollection, attributeCollection, generatedCollection, fromAttribute, toAttribute);
                    }
                    else {
                        // insert into generated edge collection with pagination
                        if(!documentCountCache.containsKey(attributeCollection)) {
                            documentCountCache.put(attributeCollection, repository.getCollectionDocumentCount(attributeCollection));
                        }
                        Integer documentCount = documentCountCache.get(attributeCollection);
                        int pageSize = (documentCount / pageCount) + 1;
                        int skip = 0;
                        for(int i = 0; i < pageCount; i++) {
                            System.out.println("Inserting into " + generatedCollection + " (page " + (i + 1) + "/" + pageCount + ")");
                            repository.insertIntoGeneratedCollectionWithPagination(fromCollection, toCollection, attributeCollection, generatedCollection, fromAttribute, toAttribute, skip, pageSize);
                            skip += pageSize;
                        }
                    }
                }
                System.out.println("Generated edge collections created!");

                //Create index on Lexeme.form and Lexeme.formNoAccent, for better performancw

                System.out.println("Creating index for collection Lexeme, field form:");
                ImportUtil.createIndex("Lexeme", List.of(new String[]{"form"}));

                System.out.println("Creating index for collection Lexeme, field formNoAccent:");
                ImportUtil.createIndex("Lexeme", List.of(new String[]{"formNoAccent"}));

                importing.compareAndSet(true, false);
                if(completeImport) {
                    optimizePartialImport(new OptimizeImportForm(pageCount));
                }
            }
            catch(Exception e) {
                //delete database content on errors
                ImportUtil.deleteCollections(repository.getCollections());
                importing.compareAndSet(true, false);
                throw e;
            }
            importing.compareAndSet(true, false);
            System.out.println("Import complete!");
            return new ResponseEntity<>("Import complete", HttpStatus.OK);
        }
    }
    @PostMapping("optimize")
    ResponseEntity<String> optimizePartialImport(@org.springframework.web.bind.annotation.RequestBody OptimizeImportForm optimizeImportForm) throws ImportException, IOException {
        Integer pageCount = optimizeImportForm.getPagecount();
        if(pageCount < 0) {
            throw new IllegalArgumentException("Page count must be a positive integer!");
        }
        if(importing.get()) {
            throw new ImportException("Import already in progress!");
        }
        else {
            importing.compareAndSet(false, true);
            System.out.println("Starting import optimization...");
            try {
                String baseRequestUrl = "http://" + arangoDbHost + ":" + arangoDbPort + "/_db/dex/_api/";
                ImportUtil.getInstance().setBaseRequestUrl(baseRequestUrl);
                ImportUtil.getInstance().setCredentials(Credentials.basic(arangoDbUser, arangoDbPassword));

                //open import schema file and store it in JsonNode for traversal

                String importSchemaFileName = "import-schema.json";
                Resource importSchemaFile = new ClassPathResource(importSchemaFileName);
                InputStream importSchemaInputStream = importSchemaFile.getInputStream();
                JsonNode importSchema = ImportUtil.getInstance().getObjectMapper().readTree(importSchemaInputStream);
                importSchemaInputStream.close();

                String finalSchemaFileName = "final-schema.json";
                Resource finalSchemaFile = new ClassPathResource(finalSchemaFileName);
                InputStream finalSchemaInputStream = finalSchemaFile.getInputStream();
                JsonNode finalSchema = ImportUtil.getInstance().getObjectMapper().readTree(finalSchemaInputStream);
                finalSchemaInputStream.close();

                //store schema colections, only SQL related to these will be parsed
                Iterator<String> importSchemaCollectionIterator;
                if (importSchema.has("collections")) {
                    importSchemaCollectionIterator = importSchema.get("collections").fieldNames();
                } else {
                    throw new ImportException("Schema document " + importSchemaFileName + " is missing field 'collections'!");
                }
                Iterator<String> importSchemaEdgeCollectionIterator = importSchema.get("edgeCollections").fieldNames();
                if (importSchema.has("edgeCollections")) {
                    importSchemaEdgeCollectionIterator = importSchema.get("edgeCollections").fieldNames();
                } else {
                    throw new ImportException("Schema document " + importSchemaFileName + " is missing field `edgeCollections`!");
                }
                Iterator<String> importSchemaGeneratedEdgeCollectionIterator;
                if (importSchema.has("generatedEdgeCollections")) {
                    importSchemaGeneratedEdgeCollectionIterator = importSchema.get("generatedEdgeCollections").fieldNames();
                } else {
                    throw new ImportException("Schema document " + importSchemaFileName + " is missing field `generatedEdgeCollections`!");
                }
                Iterator<String> finalSchemaCollectionIterator;
                if (finalSchema.has("collections")) {
                    finalSchemaCollectionIterator = finalSchema.get("collections").fieldNames();
                } else {
                    throw new ImportException("Final schema document " + finalSchemaFileName + " is missing field 'collections'!");
                }
                Iterator<String> finalSchemaEdgeCollectionIterator;
                if (finalSchema.has("edgeCollections")) {
                    finalSchemaEdgeCollectionIterator = finalSchema.get("edgeCollections").fieldNames();
                } else {
                    throw new ImportException("Final schema document " + finalSchemaFileName + " is missing field 'edgeCollections'!");
                }
                HashSet<String> importSchemaCollections = new HashSet<>();
                importSchemaCollectionIterator.forEachRemaining(importSchemaCollections::add);

                HashSet<String> importSchemaEdgeCollections = new HashSet<>();
                importSchemaEdgeCollectionIterator.forEachRemaining(importSchemaEdgeCollections::add);

                HashSet<String> importSchemaGeneratedEdgeCollections = new HashSet<>();
                importSchemaGeneratedEdgeCollectionIterator.forEachRemaining(importSchemaGeneratedEdgeCollections::add);

                HashSet<String> finalSchemaCollections = new HashSet<>();
                finalSchemaCollectionIterator.forEachRemaining(finalSchemaCollections::add);

                HashSet<String> finalSchemaEdgeCollections = new HashSet<>();
                finalSchemaEdgeCollectionIterator.forEachRemaining(finalSchemaEdgeCollections::add);
                //set final schemas
                for (String finalDocumentCollection : finalSchemaCollections) {
                    String schemaJsonString = finalSchema.get("collections").get(finalDocumentCollection).toString();
                    ImportUtil.setSchema(finalDocumentCollection, schemaJsonString);
                }
                for (String finalEdgeCollection : finalSchemaEdgeCollections) {
                    String schemaJsonString = finalSchema.get("edgeCollections").get(finalEdgeCollection).get("schema").toString();
                    ImportUtil.setSchema(finalEdgeCollection, schemaJsonString);
                }
                HashMap<String, Integer> documentCountCache = new HashMap<>();
                if(!(importSchemaCollections.contains("Lexeme") && importSchemaCollections.contains("Entry") && importSchemaCollections.contains("Tree") && importSchemaCollections.contains("Meaning") && importSchemaEdgeCollections.contains("TreeEntry") && importSchemaEdgeCollections.contains("EntryLexeme") && importSchemaGeneratedEdgeCollections.contains("MeaningMeaning") && importSchemaGeneratedEdgeCollections.contains("MeaningTree"))) {
                    System.out.println("Skipping optimization: required collections are document collections Entry, Tree, Meaning, edge collections TreeEntry, EntryLexeme, generated edge collections MeaningMeaning, MeaningTree");
                }
                else {
                    // Update Lexemes
                    if (finalSchemaCollections.contains("Lexeme")) {
                        documentCountCache.put("Lexeme", repository.getCollectionDocumentCount("Lexeme"));
                        //Required collections: document collections Entry, Tree, Meaning, edge collections TreeEntry, EntryLexeme, generated edge collections MeaningMeaning, MeaningTree

                        if(finalSchema.get("collections").get("Lexeme").get("rule").get("properties").has("meanings")) {

                            System.out.println("Adding meanings to lexemes:");
                            if(pageCount == 0) {
                                repository.insertMeanings();
                            }
                            else {
                                // insert into generated edge collection with pagination
                                Integer documentCount = documentCountCache.get("Lexeme");
                                int pageSize = (documentCount / pageCount) + 1;
                                int skip = 0;
                                for(int i = 0; i < pageCount; i++) {
                                    System.out.println("Adding meanings to Lexemes (page " + (i + 1) + "/" + pageCount + ")");
                                    repository.insertMeaningsWithPagination(skip, pageSize);
                                    skip += pageSize;
                                }
                            }
                        }

                        if(finalSchema.get("collections").get("Lexeme").get("rule").get("properties").has("usageExamples")) {
                            System.out.println("Adding usage examples to lexemes:");
                            if(pageCount == 0) {
                                repository.insertUsageExamples();
                            }
                            else {
                                Integer documentCount = documentCountCache.get("Lexeme");
                                int pageSize = (documentCount / pageCount) + 1;
                                int skip = 0;
                                for(int i = 0; i < pageCount; i++) {
                                    System.out.println("Adding usage examples to Lexemes (page " + (i + 1) + "/" + pageCount + ")");
                                    repository.insertUsageExamplesWithPagination(skip, pageSize);
                                    skip += pageSize;
                                }
                            }
                        }

                        if(finalSchema.get("collections").get("Lexeme").get("rule").get("properties").has("etymologies")) {
                            System.out.println("Adding etymologies to lexemes:");
                            if(pageCount == 0) {
                                repository.insertEtymologies();
                            }
                            else {
                                Integer documentCount = documentCountCache.get("Lexeme");
                                int pageSize = (documentCount / pageCount) + 1;
                                int skip = 0;
                                for (int i = 0; i < pageCount; i++) {
                                    System.out.println("Adding etymologies to Lexemes (page " + (i + 1) + "/" + pageCount + ")");
                                    repository.insertEtymologiesWithPagination(skip, pageSize);
                                    skip += pageSize;
                                }
                            }
                        }
                        if(finalSchema.get("collections").get("Lexeme").get("rule").get("properties").has("language")) {
                            // set languages; as of now only Romanian language
                            System.out.println("Setting Romanian language:");
                            repository.setRomanianLanguage();
                        }
                    }
                    if (finalSchemaEdgeCollections.contains("Relation") && importSchemaEdgeCollections.contains("Relation")) {
                        // Update Relation
                        System.out.println("Updating Relation: ");
                        ImportUtil.createCollection("RelationTemp", true, finalSchema.get("edgeCollections").get("Relation").get("schema").toString());
                        System.out.println("Updating Relation collection:");
                        if(pageCount == 0) {
                            repository.createRelationTemp();
                        }
                        else {
                            Integer documentCount = documentCountCache.get("Lexeme");
                            int pageSize = (documentCount / pageCount) + 1;
                            int skip = 0;
                            for(int i = 0; i < pageCount; i++) {
                                System.out.println("Updating Relation collection (page " + (i + 1) + "/" + pageCount + ")");
                                repository.createRelationTempWithPagination(skip, pageSize);
                                skip += pageSize;
                            }
                        }
                        //Replace Relation with newly built collection
                        ImportUtil.deleteCollection("Relation");
                        ImportUtil.renameCollection("RelationTemp", "Relation");
                        documentCountCache.put("Relation", repository.getCollectionDocumentCount("Relation"));
                    }
                }
                //Remove collections not in final schema
                System.out.println("Removing collections not in final schema:");
                for (String importSchemaCollection : importSchemaCollections) {
                    if (!finalSchemaCollections.contains(importSchemaCollection)) {
                        ImportUtil.deleteCollection(importSchemaCollection);
                    }
                }
                for (String importSchemaEdgeCollection : importSchemaEdgeCollections) {
                    if (!finalSchemaEdgeCollections.contains(importSchemaEdgeCollection)) {
                        ImportUtil.deleteCollection(importSchemaEdgeCollection);
                    }
                }
                for (String importSchemaGeneratedEdgeCollection : importSchemaGeneratedEdgeCollections) {
                    ImportUtil.deleteCollection(importSchemaGeneratedEdgeCollection);
                }
                //Unset attributes not in final schema
                for (String finalCollection : finalSchemaCollections) {
                    for (Iterator<String> it = importSchema.get("collections").get(finalCollection).get("rule").get("properties").fieldNames(); it.hasNext(); ) {
                        String field = it.next();
                        if (!finalSchema.get("collections").get(finalCollection).get("rule").get("properties").has(field)) {
                            System.out.println("Unsetting attribute " + field + " in collection " + finalCollection);
                            repository.unsetAttribute(finalCollection, field);
                        }
                    }
                }

                //create graph with lexemes and relation, for visualisation
                List<String> fromList = new ArrayList<>(List.of("Lexeme"));
                List<String> toList = new ArrayList<>(List.of("Lexeme"));
                EdgeDefinition edgeDefinition = new EdgeDefinition("Relation", fromList, toList);
                ImportUtil.createGraph("LexemeRelationGraph", new ArrayList<>(List.of(edgeDefinition)));
                System.out.println("Import optimization complete!");
                importing.compareAndSet(true, false);
                return new ResponseEntity<>("Import optimization complete", HttpStatus.OK);
            }
            catch(Exception e) {
                ImportUtil.deleteCollections(repository.getCollections());
                importing.compareAndSet(true, false);
                throw e;
            }
        }
    }
}
