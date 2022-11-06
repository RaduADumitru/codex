package org.example.codex.controller;

import org.example.codex.forms.OptimizedMeaningForm;
import org.example.codex.enums.LexemeForm;
import org.example.codex.enums.RelationType;
import org.example.codex.forms.RelationForm;
import org.example.codex.repository.QueryRepository;
import org.example.codex.responses.EtymologyResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/codex/optimizedsearch")
public class OptimizedImportSearchController {
    private final QueryRepository repository;

    public OptimizedImportSearchController(QueryRepository repository) {
        this.repository = repository;
    }
    @PostMapping("meanings")
    ResponseEntity<List<String>> getMeanings(@RequestBody OptimizedMeaningForm optimizedMeaningForm) {
        LexemeForm lexemeForm = LexemeForm.valueOf(optimizedMeaningForm.getWordform().toUpperCase());
        return new ResponseEntity<>(repository.optimizedGetMeanings(optimizedMeaningForm.getWord(), lexemeForm.getField()), HttpStatus.OK);
    }
    @PostMapping("etymologies")
    ResponseEntity<List<EtymologyResponse>> getEtymologies(@RequestBody OptimizedMeaningForm optimizedMeaningForm) {
        LexemeForm lexemeForm = LexemeForm.valueOf(optimizedMeaningForm.getWordform().toUpperCase());
        return new ResponseEntity<>(repository.optimizedGetEtymologies(optimizedMeaningForm.getWord(), lexemeForm.getField()), HttpStatus.OK);
    }
    @PostMapping("usageexamples")
    ResponseEntity<List<String>> getUsageExamples(@RequestBody OptimizedMeaningForm optimizedMeaningForm) {
        LexemeForm lexemeForm = LexemeForm.valueOf(optimizedMeaningForm.getWordform().toUpperCase());
        return new ResponseEntity<>(repository.optimizedGetUsageExamples(optimizedMeaningForm.getWord(), lexemeForm.getField()), HttpStatus.OK);
    }
    @PostMapping("relation")
    ResponseEntity<List<String>> getWithRelation(@RequestBody RelationForm relationForm) {
        LexemeForm lexemeForm = LexemeForm.valueOf(relationForm.getWordform().toUpperCase());
        RelationType relationType = RelationType.valueOf(relationForm.getRelationtype().toUpperCase());
        return new ResponseEntity<>(repository.optimizedGetLexemesWithRelation(relationForm.getWord(), lexemeForm.getField(), relationForm.getRelationtype().toLowerCase()), HttpStatus.OK);
    }

}
