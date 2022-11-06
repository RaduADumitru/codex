package org.project.codex.responses;

public class KeyTypeResponse {
    private String key;
    private String type;

    public KeyTypeResponse(String key, String type) {
        this.key = key;
        this.type = type;
    }

    public KeyTypeResponse() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "KeyTypeResponse{" +
                "key='" + key + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
