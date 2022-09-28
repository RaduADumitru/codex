package org.example.codex.controller;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import okhttp3.*;
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
//        Statements stmt = CCJSqlParserUtil.parseStatements(stream);
        ImportStatementListener listener = new ImportStatementListener();
        CCJSqlParserUtil.streamStatements(listener, stream, "UTF-8");
//        Test
//        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
//        String line1 = reader.readLine(); //Working
//        return new ResponseEntity<>(line1, HttpStatus.OK);
        return new ResponseEntity<>("Import complete", HttpStatus.OK);
    }

}
