package org.example.codex.controller;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import okhttp3.*;
import org.apache.poi.util.ReplacingInputStream;
import org.example.codex.util.ImportStatementListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URL;
import java.util.Objects;
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
        URL url = new URL("https://dexonline.ro/static/download/dex-database.sql.gz");
        InputStream stream = new GZIPInputStream(url.openStream());
        ReplacingInputStream replacedStream = new ReplacingInputStream(
                new ReplacingInputStream(
                new ReplacingInputStream(
                new ReplacingInputStream(
                new ReplacingInputStream(
                        stream, "UNLOCK TABLES;", "/**/"), //Cannot be parsed by JSQLParser, and is redundant
                        "LOCK", "/*"),
                        "WRITE;", "*/"),
                        // Parser does not support semicolons after SQL comments: /* ... */;
                        "*/;", "*/"),
                        //Parser does not support backslash escape character, only doubles commas
                        "\\'", "''");
        //TODO out of memory
        ImportStatementListener listener = new ImportStatementListener();
        CCJSqlParserUtil.streamStatements(listener, replacedStream, "UTF-8");
        return new ResponseEntity<>("Import complete", HttpStatus.OK);
    }

}
