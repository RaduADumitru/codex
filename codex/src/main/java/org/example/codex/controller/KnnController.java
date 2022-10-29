package org.example.codex.controller;

import org.example.codex.enums.Distance;
import org.example.codex.enums.LexemeForm;
import org.example.codex.enums.NgramDistance;
import org.example.codex.forms.DistanceForm;
import org.example.codex.forms.NgramForm;
import org.example.codex.repository.LexemeAndSystemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/codex/knn")
public class KnnController {
    private final LexemeAndSystemRepository repository;

    public KnnController(LexemeAndSystemRepository repository) {
        this.repository = repository;
    }
    @PostMapping("editdistance")
    ResponseEntity<List<String>> getEditDistance(@RequestBody DistanceForm distanceForm) {
        Distance distance = Distance.valueOf(distanceForm.getDistancetype().toUpperCase());
        LexemeForm lexemeForm = LexemeForm.valueOf(distanceForm.getWordform().toUpperCase());
        if(distance == Distance.LEVENSHTEIN) {
                return new ResponseEntity<>(repository.getKnnLevenshtein(distanceForm.getWord(), lexemeForm.getField(), distanceForm.getNeighborcount()), HttpStatus.OK);
        }
        else if(distance == Distance.HAMMING) {
            return new ResponseEntity<>(repository.getKnnHamming(distanceForm.getWord(), lexemeForm.getField(), distanceForm.getNeighborcount()), HttpStatus.OK);
        }
        else if(distance == Distance.LCS_DISTANCE) {
            return new ResponseEntity<>(repository.getKnnLCS(distanceForm.getWord(), lexemeForm.getField(), distanceForm.getNeighborcount()), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
    @PostMapping("ngram")
    ResponseEntity<List<String>> getNgramDistance(@RequestBody NgramForm ngramForm) {
        NgramDistance ngramDistance = NgramDistance.valueOf(ngramForm.getDistancetype().toUpperCase());
        LexemeForm lexemeForm = LexemeForm.valueOf(ngramForm.getWordform().toUpperCase());
        if(ngramDistance == NgramDistance.NGRAM_SIMILARITY) {
            return new ResponseEntity<>(repository.getKnnNgramSimilarity(ngramForm.getWord(), lexemeForm.getField(), ngramForm.getNgramsize(), ngramForm.getNeighborcount()), HttpStatus.OK);
        }
        else if(ngramDistance == NgramDistance.NGRAM_POSITIONAL_SIMILARITY) {
            return new ResponseEntity<>(repository.getKnnNgramPositionalSimilarity(ngramForm.getWord(), lexemeForm.getField(), ngramForm.getNgramsize(), ngramForm.getNeighborcount()), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
}
