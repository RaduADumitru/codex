package org.project.codex.controller;

import org.project.codex.enums.MeaningType;
import org.project.codex.enums.RelationType;
import org.project.codex.forms.LevenshteinForm;
import org.project.codex.forms.MeaningsForm;
import org.project.codex.forms.RegexForm;
import org.project.codex.forms.RelationForm;
import org.project.codex.repository.QueryRepository;
import org.project.codex.enums.LexemeForm;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("codex/search")
public class SearchController {
    private final QueryRepository repository;

    public SearchController(QueryRepository repository) {
        this.repository = repository;
    }

    @PostMapping("levenshtein")
    ResponseEntity<Iterable<String>> getWithLevenshtein(@RequestBody LevenshteinForm levenshteinForm) {
        LexemeForm lexemeForm = LexemeForm.valueOf(levenshteinForm.getWordform().toUpperCase());
        return new ResponseEntity<>(repository.getWithLevenshtein(levenshteinForm.getWord(), levenshteinForm.getDistance(), lexemeForm.getField()), HttpStatus.OK);
    }

    @PostMapping("regex")
    ResponseEntity<Iterable<String>> getWithRegex(@RequestBody RegexForm regexForm) {
        LexemeForm lexemeForm = LexemeForm.valueOf(regexForm.getWordform().toUpperCase());
        return new ResponseEntity<>(repository.getWithRegex(regexForm.getRegex(), lexemeForm.getField()), HttpStatus.OK);
    }

    @PostMapping("meanings")
    ResponseEntity<Iterable<String>> getMeanings(@RequestBody MeaningsForm meaningsForm) {
        LexemeForm lexemeForm = LexemeForm.valueOf(meaningsForm.getWordform().toUpperCase());
        MeaningType meaningType = MeaningType.valueOf(meaningsForm.getMeaningtype().toUpperCase());
        return new ResponseEntity<>(repository.getMeanings(meaningsForm.getWord(), meaningType.getMeaningCode(), lexemeForm.getField()), HttpStatus.OK);
    }

    @PostMapping("relation")
    ResponseEntity<Iterable<String>> getWithRelation(@RequestBody RelationForm relationForm) {
        LexemeForm lexemeForm = LexemeForm.valueOf(relationForm.getWordform().toUpperCase());
        RelationType relationType = RelationType.valueOf(relationForm.getRelationtype().toUpperCase());
        return new ResponseEntity<>(repository.getLexemesWithRelation(relationForm.getWord(), relationType.getRelationTypeCode(), lexemeForm.getField()), HttpStatus.OK);
    }
}
