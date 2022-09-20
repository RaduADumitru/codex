package org.example.codex;

import com.arangodb.ArangoDB;
import com.arangodb.springframework.annotation.EnableArangoRepositories;
import com.arangodb.springframework.config.ArangoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableArangoRepositories(basePackages = {"org.example.codex"})
public class CodexConfiguration implements ArangoConfiguration {

    @Override
    public ArangoDB.Builder arango() {
        // return new ArangoDB.Builder().host("localhost", 8529).user("root").password("openSesame");
        return new ArangoDB.Builder();
    }

    @Override
    public String database() {
        return "dex";
    }

}