package org.project.codex;

import com.arangodb.ArangoDB;
import com.arangodb.springframework.annotation.EnableArangoRepositories;
import com.arangodb.springframework.config.ArangoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableArangoRepositories(basePackages = {"org.project.codex"})
public class CodexConfiguration implements ArangoConfiguration {

    @Override
    public ArangoDB.Builder arango() {
        //loads properties from arangodb.properties file
        return new ArangoDB.Builder();
    }

    @Override
    public String database() {
        return "dex";
    }

}