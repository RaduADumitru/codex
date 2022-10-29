package org.example.codex.controller;

import org.example.codex.enums.LexemeForm;
import org.example.codex.enums.MeaningType;
import org.example.codex.enums.RelationType;
import org.example.codex.forms.*;
import org.example.codex.repository.LexemeAndSystemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("codex/search")
public class SearchController {
    private final LexemeAndSystemRepository repository;

    public SearchController(LexemeAndSystemRepository repository) {
        this.repository = repository;
    }

    @PostMapping("levenshtein")
    ResponseEntity<Iterable<String>> getWithLevenshtein(@RequestBody LevenshteinForm levenshteinForm) {
        LexemeForm lexemeForm = LexemeForm.valueOf(levenshteinForm.getWordform().toUpperCase());
        return new ResponseEntity<>(repository.getWithLevenshtein(levenshteinForm.getWord(), levenshteinForm.getDistance(), lexemeForm.getField()), HttpStatus.OK);
//        if(levenshteinForm.getWordform() == 0) {
//            return repository.getWithLevenshtein(levenshteinForm.getWord(), levenshteinForm.getDistance(), "formNoAccent");
//        }
//        else if(levenshteinForm.getWordform() == 1) {
//            return repository.getWithLevenshtein(levenshteinForm.getWord(), levenshteinForm.getDistance(), "formUtf8General");
//        }
//        else if(levenshteinForm.getWordform() == 2) {
//            return repository.getWithLevenshtein(levenshteinForm.getWord(), levenshteinForm.getDistance(), "form");
//        }
//        //Error
//        else return null;
    }

    @PostMapping("regex")
    ResponseEntity<Iterable<String>> getWithRegex(@RequestBody RegexForm regexForm) {
        LexemeForm lexemeForm = LexemeForm.valueOf(regexForm.getWordform().toUpperCase());
        return new ResponseEntity<>(repository.getWithRegex(regexForm.getRegex(), lexemeForm.getField()), HttpStatus.OK);
//        if(regexForm.getWordform() == 0) {
//            return repository.getWithRegex(regexForm.getRegex(), "formNoAccent");
//        }
//        else if(regexForm.getWordform() == 1) {
//            return repository.getWithRegex(regexForm.getRegex(), "formUtf8General");
//        }
//        else if(regexForm.getWordform() == 2) {
//            return repository.getWithRegex(regexForm.getRegex(), "form");
//        }
//        else return null;
    }

    @PostMapping("meanings")
    ResponseEntity<Iterable<String>> getMeanings(@RequestBody MeaningsForm meaningsForm) {
        LexemeForm lexemeForm = LexemeForm.valueOf(meaningsForm.getWordform().toUpperCase());
        MeaningType meaningType = MeaningType.valueOf(meaningsForm.getMeaningtype().toUpperCase());
        return new ResponseEntity<>(repository.getMeanings(meaningsForm.getWord(), meaningType.getMeaningCode(), lexemeForm.getField()), HttpStatus.OK);
//        if(meaningsForm.getCollation() == 0) {
//            return repository.getMeanings(meaningsForm.getWord(), meaningsForm.getType(), "formNoAccent");
//        }
//        else if(meaningsForm.getCollation() == 1) {
//            return repository.getMeanings(meaningsForm.getWord(), meaningsForm.getType(), "formUtf8General");
//        }
//        else if(meaningsForm.getCollation() == 2) {
//            return repository.getMeanings(meaningsForm.getWord(), meaningsForm.getType(), "form");
//        }
//        else return null;
    }

    @PostMapping("relation")
    ResponseEntity<Iterable<String>> getWithRelation(@RequestBody RelationForm relationForm) {
        LexemeForm lexemeForm = LexemeForm.valueOf(relationForm.getWordform().toUpperCase());
        RelationType relationType = RelationType.valueOf(relationForm.getRelationtype().toUpperCase());
        return new ResponseEntity<>(repository.getLexemesWithRelation(relationForm.getWord(), relationType.getRelationTypeCode(), lexemeForm.getField()), HttpStatus.OK);
//        if(relationForm.getCollation() == 0) {
//            return repository.getLexemesWithRelation(relationForm.getWord(), relationForm.getRelationType(), "formNoAccent");
//        }
//        else if(relationForm.getCollation() == 1) {
//            return repository.getLexemesWithRelation(relationForm.getWord(), relationForm.getRelationType(), "formUtf8General");
//        }
//        else if(relationForm.getCollation() == 2) {
//            return repository.getLexemesWithRelation(relationForm.getWord(), relationForm.getRelationType(), "form");
//        }
//        //Else exception
//        else return null;
    }
}
