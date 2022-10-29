package org.example.codex.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@PropertySource(value = "classpath:arangodb.properties")
public class ArangoConfiguration {
    @Value("${arangodb.host}")
    private String arangoDbHost;
    @Value("${arangodb.port}")
    private Integer arangoDbPort;
    @Value("${arangodb.user}")
    private String arangoDbUser;
    @Value("${arangodb.password}")
    private String arangoDbPassword;

    public String getArangoDbHost() {
        return arangoDbHost;
    }

    public void setArangoDbHost(String arangoDbHost) {
        this.arangoDbHost = arangoDbHost;
    }

    public Integer getArangoDbPort() {
        return arangoDbPort;
    }

    public void setArangoDbPort(Integer arangoDbPort) {
        this.arangoDbPort = arangoDbPort;
    }

    public String getArangoDbUser() {
        return arangoDbUser;
    }

    public void setArangoDbUser(String arangoDbUser) {
        this.arangoDbUser = arangoDbUser;
    }

    public String getArangoDbPassword() {
        return arangoDbPassword;
    }

    public void setArangoDbPassword(String arangoDbPassword) {
        this.arangoDbPassword = arangoDbPassword;
    }
}
