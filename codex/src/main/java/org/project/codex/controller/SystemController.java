package org.project.codex.controller;

import org.project.codex.forms.CollectionForm;
import org.project.codex.repository.QueryRepository;
import org.project.codex.responses.EdgeResponse;
import org.project.codex.responses.KeyTypeResponse;
import org.project.codex.responses.Schema;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("codex/system")
public class SystemController {
    private final QueryRepository repository;

    public SystemController(QueryRepository repository) {
        this.repository = repository;
    }
    @GetMapping("schema/collections")
    Iterable<String> getCollections() {
        return repository.getCollections();
    }
    @PostMapping("collection/key_types")
    Iterable<KeyTypeResponse> getKeyTypes(@RequestBody CollectionForm collectionForm) {
        return repository.getKeyTypes(collectionForm.getCollection());
    }
    @PostMapping("collection/is_edge_collection")
    public Boolean isEdgeCollection(@RequestBody CollectionForm collectionForm) {
        return repository.isEdgeCollection(collectionForm.getCollection());
    }
    @PostMapping("collection/edge_relations")
    List<EdgeResponse> getEdgeRelations(@RequestBody CollectionForm collectionForm) {
        return repository.getEdgeRelations(collectionForm.getCollection());
    }
    @GetMapping("schema/key_types_all")
    HashMap<String, List<KeyTypeResponse>> getKeyTypesAll() {
        Iterable<String> collections = repository.getCollections();
        HashMap<String, List<KeyTypeResponse>> keyTypeMap = new HashMap<>();
        for (String col:collections) {
            keyTypeMap.put(col, repository.getKeyTypes(col));
        }
        return keyTypeMap;
    }
    @GetMapping("schema/edge_relations_all")
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
    @GetMapping("schema/schema")
    Schema getSchema() {
        Schema schema = new Schema();
        schema.setKeyTypeMap(getKeyTypesAll());
        schema.setEdgeRelationMap(getEdgeRelationsAll());
        return schema;
    }
}
