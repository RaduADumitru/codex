package org.example.codex.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.ReplacingInputStream;
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
    ResponseEntity<String> databaseImport() throws IOException, JSQLParserException {
        //open import schema file and store it in JsonNode for traversal
        Resource importSchemaFile = new ClassPathResource("import-schema.json");
        InputStream inputStream = importSchemaFile.getInputStream();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode importSchema = mapper.readTree(inputStream);
        //store schema colections, only SQL related to these will be parsed
        Iterator<String> collectionIterator = importSchema.get("collections").fieldNames();
        HashSet<String> collections = new HashSet<>();
        collectionIterator.forEachRemaining(collections::add);
        //load archived database SQL script from remote link
        URL url = new URL("https://dexonline.ro/static/download/dex-database.sql.gz");
        InputStream stream = new GZIPInputStream(url.openStream());
        Scanner sc = new Scanner(stream, StandardCharsets.UTF_8);
        //create statements will be built starting with lines beginning with "CREATE" and ending with ";""
        boolean buildingCreateStatement = false;
        StringBuilder createStatement = new StringBuilder();
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


//                    System.out.println(String.valueOf(createStatement));
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
                        line = line.replace("\\'", "''");
                        Insert insert = (Insert)CCJSqlParserUtil.parse(line);
                        System.out.println("INSERT: " + insert.getTable().getName());
                    }
                }
                else if(line.startsWith("CREATE")) {
                    //begin building create statement to parse afterwards
                    buildingCreateStatement = true;
                    createStatement.append(line);
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

}
