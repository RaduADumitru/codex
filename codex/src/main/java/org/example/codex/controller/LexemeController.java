package org.example.codex.controller;

import org.example.codex.forms.LevenshteinForm;
import org.example.codex.forms.RegexForm;
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
}
