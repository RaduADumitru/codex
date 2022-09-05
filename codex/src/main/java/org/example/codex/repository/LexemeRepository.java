package org.example.codex.repository;

import com.arangodb.springframework.annotation.Query;
import com.arangodb.springframework.repository.ArangoRepository;
import org.example.codex.model.Lexeme;
import org.springframework.data.repository.query.Param;

public interface LexemeRepository extends ArangoRepository<Lexeme, String> {
    @Query("""
                  // returns all words at a Levenshtein distance maximum of @dist from @word
                  for lexeme in Lexeme
                  
                  // redundant to return same word as input
                  filter lexeme.formUtf8General not like @word
                  
                  filter levenshtein_distance(@word, lexeme.formUtf8General) <= @dist
                  
                  // return distinct since multiple lexemes can have same forms
                  return distinct lexeme.formUtf8General
                  """)
    Iterable<String> getWithLevenshtein(@Param("word") String word, @Param("dist") Integer dist);
    @Query("""
                 for lexeme in Lexeme
                     
                     // Get words matching regex
                     filter lexeme.formUtf8General like @regex
                     
                     // return distinct since multiple lexemes can have same forms
                     return distinct lexeme.formUtf8General
                  """)
    Iterable<String> getWithRegex(@Param("regex") String regex);
    @Query("""
    // Get Meanings of lexeme
    //Multiple lexemes can have same form, so start from each of them
    LET start_vertex = (
        for l in Lexeme
        filter l.formUtf8General like "alb"
        return l
    )

    //Meanings are structured as trees, roots have parent Id 0, so select them to start traversal from there
    LET meaning_tree_roots = (
    for start in start_vertex
        //Graph containing Lexeme <- Entry <- Meaning collections
        for v, e, p in 1..3 inbound start GRAPH LexemeMeaningRoot
        filter p.vertices[3].parentId == 0
        return p.vertices[3])
    for root in meaning_tree_roots
        //Traverse tree to the end and return each meaning
        //10 is arbitrary number so that traversal doesn't stop beforehand, no path has a length of more than 10

        //Graph MeaningGraph containing meanings: parent -> child
        for v, e, p in 0..10 outbound root GRAPH MeaningGraph
        //types:
        // 0 - proper meaning
        //1 - etymology
        //2 - usage example from literature
        //3 - comment
        //4 - diff from parent meaning
        //5 - compound expression
        filter v.type == 0
        // regex_replace to eliminate dollars/parantheses with IDs used for DexOnline backend?
        //some Meanings have no definitions, avoid showing null
        filter v.internalRep != null
        return v.internalRep
""")
    Iterable<String> getMeanings(@Param("word") String word, @Param("type") Integer type);
}