package org.example.codex.controller;

import org.example.codex.forms.LevenshteinForm;
import org.example.codex.forms.MeaningsForm;
import org.example.codex.forms.RegexForm;
import org.example.codex.forms.RelationForm;
import org.example.codex.repository.LexemeAndSystemRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("codex/lexeme")
public class LexemeController {
    private final LexemeAndSystemRepository repository;

    public LexemeController(LexemeAndSystemRepository repository) {
        this.repository = repository;
    }

    @PostMapping("levenshtein")
    Iterable<String> getWithLevenshtein(@RequestBody LevenshteinForm levenshteinForm) {
        if(levenshteinForm.getCollation() == 0) {
            return repository.getWithLevenshtein(levenshteinForm.getWord(), levenshteinForm.getDist(), "formNoAccent");
        }
        else if(levenshteinForm.getCollation() == 1) {
            return repository.getWithLevenshtein(levenshteinForm.getWord(), levenshteinForm.getDist(), "formUtf8General");
        }
        else if(levenshteinForm.getCollation() == 2) {
            return repository.getWithLevenshtein(levenshteinForm.getWord(), levenshteinForm.getDist(), "form");
        }
        //Error
        else return null;
    }

    @PostMapping("regex")
    Iterable<String> getWithRegex(@RequestBody RegexForm regexForm) {
        if(regexForm.getCollation() == 0) {
            return repository.getWithRegex(regexForm.getRegex(), "formNoAccent");
        }
        else if(regexForm.getCollation() == 1) {
            return repository.getWithRegex(regexForm.getRegex(), "formUtf8General");
        }
        else if(regexForm.getCollation() == 2) {
            return repository.getWithRegex(regexForm.getRegex(), "form");
        }
        else return null;
    }

    @PostMapping("meanings")
    Iterable<String> getMeanings(@RequestBody MeaningsForm meaningsForm) {
        if(meaningsForm.getCollation() == 0) {
            return repository.getMeanings(meaningsForm.getWord(), meaningsForm.getType(), "formNoAccent");
        }
        else if(meaningsForm.getCollation() == 1) {
            return repository.getMeanings(meaningsForm.getWord(), meaningsForm.getType(), "formUtf8General");
        }
        else if(meaningsForm.getCollation() == 2) {
            return repository.getMeanings(meaningsForm.getWord(), meaningsForm.getType(), "form");
        }
        else return null;
    }

    @PostMapping("relation")
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
