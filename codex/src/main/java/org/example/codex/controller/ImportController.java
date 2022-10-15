package org.example.codex.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jdi.FloatValue;
import com.sun.source.tree.Tree;
import net.minidev.json.JSONArray;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.function.Exp;
import org.apache.poi.util.ReplacingInputStream;
import org.example.codex.enums.ArangoDataTypes;
import org.example.codex.exceptions.ImportException;
import org.example.codex.forms.CollectionForm;
import org.example.codex.repository.LexemeAndSystemRepository;
import org.example.codex.util.ColumnData;
import org.example.codex.util.ImportStatementListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

@RestController
@RequestMapping("/codex/import/")
public class ImportController {

    private final LexemeAndSystemRepository repository;
    public ImportController(LexemeAndSystemRepository repository) {
        this.repository = repository;
    }
    String baseRequestUrl = "http://localhost:8529/_db/dex/_api/";
    ObjectMapper mapper = new ObjectMapper();
    OkHttpClient client = new OkHttpClient();
    String credential = Credentials.basic("root", "openSesame");
    //Test sending request to DB API; working
    @GetMapping("version")
    ResponseEntity<String> getVersion() throws IOException {
        OkHttpClient client = new OkHttpClient();
        String credential = Credentials.basic("root", "openSesame");
        Request request = new Request.Builder()
                .url("http://localhost:8529/_api/version")
                .addHeader("Content-Type", "application/json")
                .header("Authorization", credential)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return new ResponseEntity<>(Objects.requireNonNull(response.body()).string(), HttpStatus.OK);
    }

    @PostMapping("import")
    ResponseEntity<String> databaseImport() throws IOException, JSQLParserException, ImportException {
        //DATA TYPES:
        //ArangoDB/JSON has three main data types: boolean, number and string: SQL types will be converted to these
        final HashSet<String> stringDataTypes = new HashSet<>(List.of(new String[]{"varchar", "char", "mediumtext", "longtext", "date", "timestamp"}));
        final HashSet<String> numberDataTypes = new HashSet<>(List.of(new String[]{"int", "bigint", "smallint", "float"}));
        final HashSet<String> booleanDataTypes = new HashSet<>(List.of(new String[]{"tinyint"}));
        final HashMap<String, ArangoDataTypes> typeMap = new HashMap<String, ArangoDataTypes>(
                Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, ArangoDataTypes>("varchar", ArangoDataTypes.STRING),
                        new AbstractMap.SimpleEntry<String, ArangoDataTypes>("char", ArangoDataTypes.STRING),
                        new AbstractMap.SimpleEntry<String, ArangoDataTypes>("mediumtext", ArangoDataTypes.STRING),
                        new AbstractMap.SimpleEntry<String, ArangoDataTypes>("longtext", ArangoDataTypes.STRING),
                        new AbstractMap.SimpleEntry<String, ArangoDataTypes>("date", ArangoDataTypes.STRING),
                        new AbstractMap.SimpleEntry<String, ArangoDataTypes>("timestamp", ArangoDataTypes.STRING),
                        new AbstractMap.SimpleEntry<String, ArangoDataTypes>("int", ArangoDataTypes.NUMBER),
                        new AbstractMap.SimpleEntry<String, ArangoDataTypes>("bigint", ArangoDataTypes.NUMBER),
                        new AbstractMap.SimpleEntry<String, ArangoDataTypes>("smallint", ArangoDataTypes.NUMBER),
                        new AbstractMap.SimpleEntry<String, ArangoDataTypes>("float", ArangoDataTypes.NUMBER),
                        new AbstractMap.SimpleEntry<String, ArangoDataTypes>("tinyint", ArangoDataTypes.BOOLEAN)
                )
        );
        //open import schema file and store it in JsonNode for traversal

        ObjectMapper mapper = new ObjectMapper();

        Resource importSchemaFile = new ClassPathResource("import-schema.json");
        InputStream importSchemaInputStream = importSchemaFile.getInputStream();
        JsonNode importSchema = mapper.readTree(importSchemaInputStream);
        importSchemaInputStream.close();

        Resource finalSchemaFile = new ClassPathResource("final-schema.json");
        InputStream finalSchemaInputStream = finalSchemaFile.getInputStream();
        JsonNode finalSchema = mapper.readTree(finalSchemaInputStream);
        finalSchemaInputStream.close();

        //store schema colections, only SQL related to these will be parsed
        Iterator<String> collectionIterator = importSchema.get("collections").fieldNames();
        Iterator<String> edgeCollectionIterator = importSchema.get("edgeCollections").fieldNames();

        HashSet<String> collections = new HashSet<>();
        collectionIterator.forEachRemaining(collections::add);
        System.out.println(collections.toString());

        HashSet<String> edgeCollections = new HashSet<>();
        edgeCollectionIterator.forEachRemaining(edgeCollections::add);

        //for each collection, store columns in schema along with their data types
//        Map<String, Map<String, ArangoDataTypes>> colDataTypes = new HashMap<>();
        HashMap<String, HashMap<String, ColumnData>> SqlColumnPositionMap = new HashMap<>();
        HashMap<String, TreeSet<ColumnData>> colDataMap = new HashMap<>();

        StringBuilder insertRequestBody = new StringBuilder();

        //load archived database SQL script from remote link
        URL url = new URL("https://dexonline.ro/static/download/dex-database.sql.gz");
        InputStream stream = new GZIPInputStream(url.openStream());
        Scanner sc = new Scanner(stream, StandardCharsets.UTF_8);
        //create statements will be built starting with lines beginning with "CREATE" and ending with ";""
        boolean buildingCreateStatement = false;
        StringBuilder createStatement = new StringBuilder();

        //Delete all collections, so import is fresh
        List<String> oldCollections = repository.getCollections();
        for(String col : oldCollections) {
            String deleteURL = baseRequestUrl + "collection/" + col;
            Request request = new Request.Builder()
                    .url(deleteURL)
                    .addHeader("Accept", "application/json")
                    .header("Authorization", credential)
                    .delete()
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            System.out.println("Delete: " + col);
            System.out.println(Objects.requireNonNull(response.body()).string());
        }
        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            if(buildingCreateStatement) {
                //check if line reached is not end of create statement
                if(line.indexOf(';') == -1) {
                    createStatement.append(line);
                }
                else {
                    createStatement.append(");");
                    //parse finished statement and send request
                    CreateTable createTable = (CreateTable)CCJSqlParserUtil.parse(String.valueOf(createStatement));
                    //delete collection, then create it while applying JsonSchema
                    String collectionName = createTable.getTable().getName();
//                    List<ColumnDefinition> columnDefinitions = createTable.getColumnDefinitions();
//                    for(ColumnDefinition columnDefinition : columnDefinitions) {
//                        System.out.println("Column name: " + columnDefinition.getColumnName());
//                        System.out.println("Column spec: " + columnDefinition.getColumnSpecs().toString());
//                        System.out.println("Column type: " + columnDefinition.getColDataType().getDataType());
//                    }
                    //Will be stored with apostrophes; need to be removed
                    collectionName = collectionName.replace("`", "");
                    //if collection is in schema, create it as normal collection
                    System.out.println("CREATE " + collectionName + "FINISHED PARSING");
                    System.out.println("IS " + collectionName + " IN "+ collections.toString());
                    //TODO: Create with schema
                    if(collections.contains(collectionName)) {
                        System.out.println("COLLECTIONS CONTAINS " + collectionName);
                        String requestURL = baseRequestUrl + "collection";
                        ObjectNode CreateRequestJsonObject = mapper.createObjectNode();
                        CreateRequestJsonObject.put("name", collectionName);
                        String jsonString = mapper.writeValueAsString(CreateRequestJsonObject);
                        System.out.println("Create request string: " + jsonString);
                        RequestBody formBody = FormBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
                        Request request = new Request.Builder()
                                .url(requestURL)
                                .addHeader("Content-Type", "application/json")
                                .addHeader("Accept", "application/json")
                                .header("Authorization", credential)
                                .post(formBody)
                                .build();
                        Call call = client.newCall(request);
                        Response response = call.execute();
                        System.out.println(Objects.requireNonNull(response.body()).string());

                        // Memorise indexes of each column in collection
                        SqlColumnPositionMap.put(collectionName, new HashMap<>());
                        List<ColumnDefinition> columnDefinitions = createTable.getColumnDefinitions();
                        Integer columnIndex = 0;
                        //Store position of each column in map
                        for(ColumnDefinition columnDefinition : columnDefinitions) {
                            String columnName = columnDefinition.getColumnName().replace("`", "");
                            String columnDataType = columnDefinition.getColDataType().getDataType();
                            SqlColumnPositionMap.get(collectionName).put(columnName, new ColumnData(columnName, columnIndex, typeMap.get(columnDataType)));
                            columnIndex++;
                        }

                        //add data types of columns in schema for this collection

                        colDataMap.put(collectionName, new TreeSet<>());
                        Iterator<String> fieldsIterator = importSchema.get("collections").get(collectionName).get("rule").get("properties").fieldNames();
                        while (fieldsIterator.hasNext()) {
                            String fieldName = fieldsIterator.next();
                            //throw exception if field in schema doesn't exist in database
//                            boolean databaseHasSchemaField = false;
//                            for(ColumnDefinition columnDefinition : columnDefinitions) {
//                                String columnName = columnDefinition.getColumnName().replace("`", "");
//                                if(columnName.equals(fieldName)) {
//                                    databaseHasSchemaField = true;
//                                    String columnDataType = columnDefinition.getColDataType().getDataType();
////                                    colDataMap.get(collectionName).put(columnName, typeMap.get(columnDataType));
//                                    Integer columnPosition = SqlColumnPositionMap.get(collectionName).get(columnName);
//                                    colDataMap.get(collectionName).add(new ColumnData(columnPosition, typeMap.get(columnDataType)));
//                                    break;
//                                }
//                            }
                            if(SqlColumnPositionMap.get(collectionName).containsKey(fieldName)) {
                                Integer columnPosition = SqlColumnPositionMap.get(collectionName).get(fieldName).getPosition();
                                ArangoDataTypes columnDataType = SqlColumnPositionMap.get(collectionName).get(fieldName).getDataType();
                                colDataMap.get(collectionName).add(new ColumnData(fieldName, columnPosition, columnDataType));
                            }
                            else {
                                throw new ImportException("Column " + fieldName + " in collection " + collectionName + " in import schema doesn't exist in database!");
                            }
                        }
                    }
//                    System.out.println(String.valueOf(createStatement));
                    //Start building insert request body: first row represents column names inserted
                    ArrayList<String> insertColumns = new ArrayList<>();
                    //key will always be imported first
                    insertColumns.add("_key");
                    for(ColumnData colData : colDataMap.get(collectionName)) {
                        insertColumns.add(colData.getColumnName());
                    }
                    insertRequestBody.append(JSONArray.toJSONString(insertColumns)).append("\n");
                    //reset built statement
                    createStatement = new StringBuilder();
                    buildingCreateStatement = false;
                    System.out.println("CREATE: " + createTable.getTable().getName());
                }
            }
            else {
                if(line.startsWith("INSERT")) {
                    String table = StringUtils.substringBetween(line, "INTO `", "` VALUES");
                    //statement will only be parsed if collection is in schema
                    if(collections.contains(table)) {
                        //JSQLparser does not support backslash escape character in statements, only double commas
                        line = line.replace("\\'", "''");
                        Insert insert = (Insert)CCJSqlParserUtil.parse(line);
                        ItemsList itemsList = insert.getItemsList();
                        if(itemsList instanceof MultiExpressionList multiExpressionList) {
                            for (ExpressionList expressionList : multiExpressionList.getExpressionLists()) {
                                //where values for a row will be stored
                                ArrayList insertRequestBodyRowItems = new ArrayList();
                                List<Expression> expressions = expressionList.getExpressions();
                                for (ColumnData colData : colDataMap.get(table)) {
//                                    insertColumns.add(colData.getColumnName());
                                    Expression expression = expressions.get(colData.getPosition());
                                    ArangoDataTypes dataType = colData.getDataType();
                                    if (expression instanceof StringValue) {
                                        if (colData.getDataType().equals(ArangoDataTypes.STRING)) {
                                            insertRequestBodyRowItems.add(((StringValue) expression).getValue());
                                        } else {
                                            throw new ImportException("Import schema type mismatch: collection " + table + " attribute " + colData.getColumnName() + ": expected String!");
                                        }
                                    } else if (expression instanceof DoubleValue) {
                                        if (colData.getDataType().equals(ArangoDataTypes.NUMBER)) {
                                            insertRequestBodyRowItems.add((((DoubleValue) expression).getValue()));
                                        } else {
                                            throw new ImportException("Import schema type mismatch: collection " + table + " attribute " + colData.getColumnName() + ": expected Double!");
                                        }
                                    } else if (expression instanceof LongValue) {
                                        if (colData.getDataType().equals(ArangoDataTypes.NUMBER)) {
                                            insertRequestBodyRowItems.add((((LongValue) expression).getValue()));
                                        } else if (colData.getDataType().equals(ArangoDataTypes.BOOLEAN)) {
                                            long longValue = ((LongValue) expression).getValue();
                                            if (longValue == 0) {
                                                insertRequestBodyRowItems.add(false);
                                            } else if (longValue == 1) {
                                                insertRequestBodyRowItems.add(true);
                                            } else {
                                                throw new ImportException("Import schema type mismatch: collection " + table + " attribute " + colData.getColumnName() + ": expected Boolean!");
                                            }
                                        }
                                    } else if (expression instanceof NullValue) {
                                        insertRequestBodyRowItems.add(null);
                                    }
                                    else if(expression instanceof DateValue) {
                                        if(colData.getDataType().equals(ArangoDataTypes.STRING)) {
                                            insertRequestBodyRowItems.add(((DateValue)expression).getValue().toString());
                                        }
                                        else {
                                            throw new ImportException("Import schema type mismatch: collection " + table + " attribute " + colData.getColumnName() + ": expected Date!");
                                        }
                                    }
                                    else if(expression instanceof TimeValue) {
                                        if(colData.getDataType().equals(ArangoDataTypes.STRING)) {
                                            insertRequestBodyRowItems.add(((TimeValue)expression).getValue().toString());
                                        }
                                        else {
                                            throw new ImportException("Import schema type mismatch: collection " + table + " attribute " + colData.getColumnName() + ": expected Time!");
                                        }
                                    }
                                    else if(expression instanceof TimestampValue) {
                                        if(colData.getDataType().equals(ArangoDataTypes.STRING)) {
                                            insertRequestBodyRowItems.add(((TimestampValue)expression).getValue().toString());
                                        }
                                        else {
                                            throw new ImportException("Import schema type mismatch: collection " + table + " attribute " + colData.getColumnName() + ": expected Timestamp!");
                                        }
                                    }
                                    else {
                                        throw new ImportException("SQL parsing error: type could not be determined!");

                                    }
                                }
                                insertRequestBody.append(JSONArray.toJSONString(insertRequestBodyRowItems)).append("\n");
                            }
                        }
                        else if(itemsList instanceof ExpressionList){
                            ExpressionList expressionList = (ExpressionList)itemsList;
                            //where values for a row will be stored
                            ArrayList insertRequestBodyRowItems = new ArrayList();
                            List<Expression> expressions = expressionList.getExpressions();
                            for (ColumnData colData : colDataMap.get(table)) {
//                                    insertColumns.add(colData.getColumnName());
                                Expression expression = expressions.get(colData.getPosition());
                                ArangoDataTypes dataType = colData.getDataType();
                                if (expression instanceof StringValue) {
                                    if (colData.getDataType().equals(ArangoDataTypes.STRING)) {
                                        insertRequestBodyRowItems.add(((StringValue) expression).getValue());
                                    } else {
                                        throw new ImportException("Import schema type mismatch: collection " + table + " attribute " + colData.getColumnName() + ": expected String!");
                                    }
                                } else if (expression instanceof DoubleValue) {
                                    if (colData.getDataType().equals(ArangoDataTypes.NUMBER)) {
                                        insertRequestBodyRowItems.add((((DoubleValue) expression).getValue()));
                                    } else {
                                        throw new ImportException("Import schema type mismatch: collection " + table + " attribute " + colData.getColumnName() + ": expected Double!");
                                    }
                                } else if (expression instanceof LongValue) {
                                    if (colData.getDataType().equals(ArangoDataTypes.NUMBER)) {
                                        insertRequestBodyRowItems.add((((LongValue) expression).getValue()));
                                    } else if (colData.getDataType().equals(ArangoDataTypes.BOOLEAN)) {
                                        long longValue = ((LongValue) expression).getValue();
                                        if (longValue == 0) {
                                            insertRequestBodyRowItems.add(false);
                                        } else if (longValue == 1) {
                                            insertRequestBodyRowItems.add(true);
                                        } else {
                                            throw new ImportException("Import schema type mismatch: collection " + table + " attribute " + colData.getColumnName() + ": expected Boolean!");
                                        }
                                    }
                                } else if (expression instanceof NullValue) {
                                    insertRequestBodyRowItems.add(null);
                                }
                                else if(expression instanceof DateValue) {
                                    if(colData.getDataType().equals(ArangoDataTypes.STRING)) {
                                        insertRequestBodyRowItems.add(((DateValue)expression).getValue().toString());
                                    }
                                    else {
                                        throw new ImportException("Import schema type mismatch: collection " + table + " attribute " + colData.getColumnName() + ": expected Date!");
                                    }
                                }
                                else if(expression instanceof TimeValue) {
                                    if(colData.getDataType().equals(ArangoDataTypes.STRING)) {
                                        insertRequestBodyRowItems.add(((TimeValue)expression).getValue().toString());
                                    }
                                    else {
                                        throw new ImportException("Import schema type mismatch: collection " + table + " attribute " + colData.getColumnName() + ": expected Time!");
                                    }
                                }
                                else if(expression instanceof TimestampValue) {
                                    if(colData.getDataType().equals(ArangoDataTypes.STRING)) {
                                        insertRequestBodyRowItems.add(((TimestampValue)expression).getValue().toString());
                                    }
                                    else {
                                        throw new ImportException("Import schema type mismatch: collection " + table + " attribute " + colData.getColumnName() + ": expected Timestamp!");
                                    }
                                }
                                else {
                                    throw new ImportException("SQL parsing error: type could not be determined!");

                                }
                            }
                            insertRequestBody.append(JSONArray.toJSONString(insertRequestBodyRowItems)).append("\n");
                        }
                        System.out.println("INSERT: " + insert.getTable().getName());
                        
                    }
                }
                else {
                    //TODO: send insert request?
                    
                    
                    
                    insertRequestBody = new StringBuilder();
                    if(line.startsWith("CREATE")) {
                        //begin building create statement to parse afterwards
                        buildingCreateStatement = true;
                        createStatement.append(line);
                    }
                }
            }
        }
        System.out.println("Import complete!");
        sc.close();
        stream.close();
        //Create generated collections


//        //Former method for parsing: memory issues
//        ImportStatementListener listener = new ImportStatementListener();
//        CCJSqlParserUtil.streamStatements(listener, replacedStream, "UTF-8");
        return new ResponseEntity<>("Import complete", HttpStatus.OK);
    }

    @PostMapping("create")
    ResponseEntity<String> createCollection() throws IOException {
        ObjectNode jsonObject = mapper.createObjectNode();
        jsonObject.put("name", "TestCol");
        String jsonString = mapper.writeValueAsString(jsonObject);
        RequestBody formBody = FormBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("http://localhost:8529/_db/dex/_api/collection")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .header("Authorization", credential)
                .post(formBody)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return new ResponseEntity<>(Objects.requireNonNull(response.body()).string(), HttpStatus.OK);
    }

}
