package org.example.codex.controller;

import org.example.codex.forms.CollectionForm;
import org.example.codex.repository.LexemeAndSystemRepository;
import org.example.codex.responses.EdgeResponse;
import org.example.codex.responses.KeyTypeResponse;
import org.example.codex.responses.Schema;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class SystemController {
    private final LexemeAndSystemRepository repository;

    public SystemController(LexemeAndSystemRepository repository) {
        this.repository = repository;
    }
    @GetMapping("/system/schema/collections")
    Iterable<String> getCollections() {
        return repository.getCollections();
    }
    @PostMapping("/system/collection/key_types")
    Iterable<KeyTypeResponse> getKeyTypes(@RequestBody CollectionForm collectionForm) {
        return repository.getKeyTypes(collectionForm.getCollection());
    }
    @PostMapping("/system/collection/is_edge_collection")
    Boolean isEdgeCollection(@RequestBody CollectionForm collectionForm) {
        return repository.isEdgeCollection(collectionForm.getCollection());
    }
    @PostMapping("/system/collection/edge_relations")
    List<EdgeResponse> getEdgeRelations(@RequestBody CollectionForm collectionForm) {
        return repository.getEdgeRelations(collectionForm.getCollection());
    }
    @GetMapping("system/schema/key_types_all")
    HashMap<String, List<KeyTypeResponse>> getKeyTypesAll() {
        Iterable<String> collections = repository.getCollections();
        HashMap<String, List<KeyTypeResponse>> keyTypeMap = new HashMap<>();
        for (String col:collections) {
            keyTypeMap.put(col, repository.getKeyTypes(col));
        }
        return keyTypeMap;
    }
    @GetMapping("system/schema/edge_relations_all")
    HashMap<String, List<EdgeResponse>> getEdgeRelationsAll() {
        HashMap<String, List<EdgeResponse>> edgeRelationMap = new HashMap<>();
        Iterable<String> collections = repository.getCollections();
        ArrayList<String> edgeCollections = new ArrayList<>();
        for(String col:collections) {
            if(repository.isEdgeCollection(col)) {
                edgeCollections.add(col);
            }
        }
        for(String col:edgeCollections) {
            edgeRelationMap.put(col, repository.getEdgeRelations(col));
        }
        return edgeRelationMap;
    }
    @GetMapping("system/schema/schema")
    Schema getSchema() {
        Schema schema = new Schema();
        schema.setKeyTypeMap(getKeyTypesAll());
        schema.setEdgeRelationMap(getEdgeRelationsAll());
        return schema;
    }

}
