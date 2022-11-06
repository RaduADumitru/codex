package org.project.codex.responses;

import java.util.HashMap;
import java.util.List;

public class Schema {
    private HashMap<String, List<KeyTypeResponse>> keyTypeMap = new HashMap<>();
    private HashMap<String, List<EdgeResponse>> edgeRelationMap = new HashMap<>();

    public Schema(HashMap<String, List<KeyTypeResponse>> keyTypeMap, HashMap<String, List<EdgeResponse>> edgeRelationMap) {
        this.keyTypeMap = keyTypeMap;
        this.edgeRelationMap = edgeRelationMap;
    }

    public Schema() {
    }

    public HashMap<String, List<KeyTypeResponse>> getKeyTypeMap() {
        return keyTypeMap;
    }

    public void setKeyTypeMap(HashMap<String, List<KeyTypeResponse>> keyTypeMap) {
        this.keyTypeMap = keyTypeMap;
    }

    public HashMap<String, List<EdgeResponse>> getEdgeRelationMap() {
        return edgeRelationMap;
    }

    public void setEdgeRelationMap(HashMap<String, List<EdgeResponse>> edgeRelationMap) {
        this.edgeRelationMap = edgeRelationMap;
    }
    public void putKeyTypeMap(String key, List<KeyTypeResponse> value) {
        keyTypeMap.put(key, value);
    }
    public void putEdgeRelationMap(String key, List<EdgeResponse> value) {
        edgeRelationMap.put(key, value);
    }
}
