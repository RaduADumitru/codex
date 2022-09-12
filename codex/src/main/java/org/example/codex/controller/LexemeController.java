package org.example.codex.controller;

import org.example.codex.forms.LevenshteinForm;
import org.example.codex.forms.MeaningsForm;
import org.example.codex.forms.RegexForm;
import org.example.codex.forms.RelationForm;
import org.example.codex.repository.LexemeRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LexemeController {
    private final LexemeRepository repository;

    public LexemeController(LexemeRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/lexeme/levenshtein")
    Iterable<String> getWithLevenshtein(@RequestBody LevenshteinForm levenshteinForm) {
        return repository.getWithLevenshtein(levenshteinForm.getWord(), levenshteinForm.getDist());
    }

    @PostMapping("/lexeme/regex")
    Iterable<String> getWithRegex(@RequestBody RegexForm regexForm) {
        return repository.getWithRegex(regexForm.getRegex());
    }

    @PostMapping("/lexeme/meanings")
    Iterable<String> getMeanings(@RequestBody MeaningsForm meaningsForm) {
        return repository.getMeanings(meaningsForm.getWord(), meaningsForm.getType());
    }

    @PostMapping("/lexeme/relation")
    Iterable<String> getWithRelation(@RequestBody RelationForm relationForm) {
        if(relationForm.getCollation() == 0) {
            return repository.getLexemesWithRelation(relationForm.getWord(), relationForm.getRelationType(), "formNoAccent");
        }
        else if(relationForm.getCollation() == 1) {
            return repository.getLexemesWithRelation(relationForm.getWord(), relationForm.getRelationType(), "formUtf8General");
        }
        else if(relationForm.getCollation() == 2) {
            return repository.getLexemesWithRelation(relationForm.getWord(), relationForm.getRelationType(), "form");
        }
        //Else exception
        else return null;
    }
}
