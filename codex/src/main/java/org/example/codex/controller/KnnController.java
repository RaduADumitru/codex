package org.example.codex.controller;

import org.example.codex.enums.Distances;
import org.example.codex.enums.LexemeForms;
import org.example.codex.enums.NgramDistances;
import org.example.codex.forms.DistanceForm;
import org.example.codex.forms.NgramForm;
import org.example.codex.model.Lexeme;
import org.example.codex.repository.LexemeAndSystemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
    ResponseEntity<List<String>>  getEditDistance(DistanceForm distanceForm) {
        Distances distance = Distances.valueOf(distanceForm.getDistanceType().toUpperCase());
        LexemeForms lexemeForm = LexemeForms.valueOf(distanceForm.getForm().toUpperCase());
        if(distance == Distances.LEVENSHTEIN) {
                return new ResponseEntity<>(repository.getKnnLevenshtein(distanceForm.getWord(), lexemeForm.getField(), distanceForm.getNeighborCount()), HttpStatus.OK);
        }
        else if(distance == Distances.HAMMING) {
            return new ResponseEntity<>(repository.getKnnHamming(distanceForm.getWord(), lexemeForm.getField(), distanceForm.getNeighborCount()), HttpStatus.OK);
        }
        else if(distance == Distances.LCS_DISTANCE) {
            return new ResponseEntity<>(repository.getKnnLCS(distanceForm.getWord(), lexemeForm.getField(), distanceForm.getNeighborCount()), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
    @PostMapping("ngram")
    ResponseEntity<List<String>> getNgramDistance(NgramForm ngramForm) {
        NgramDistances ngramDistance = NgramDistances.valueOf(ngramForm.getDistanceType().toUpperCase());
        LexemeForms lexemeForm = LexemeForms.valueOf(ngramForm.getForm().toUpperCase());
        if(ngramDistance == NgramDistances.NGRAM_SIMILARITY) {
            return new ResponseEntity<>(repository.getKnnNgramSimilarity(ngramForm.getWord(), lexemeForm.getField(), ngramForm.getnGramSize(), ngramForm.getNeighborCount()), HttpStatus.OK);
        }
        else if(ngramDistance == NgramDistances.NGRAM_POSITIONAL_SIMILARITY) {
            return new ResponseEntity<>(repository.getKnnNgramPositionalSimilarity(ngramForm.getWord(), lexemeForm.getField(), ngramForm.getnGramSize(), ngramForm.getNeighborCount()), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
}
