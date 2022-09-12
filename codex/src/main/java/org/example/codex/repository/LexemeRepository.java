package org.example.codex.repository;

import com.arangodb.springframework.annotation.Query;
import com.arangodb.springframework.repository.ArangoRepository;
import org.example.codex.model.Lexeme;
import org.springframework.data.repository.query.Param;

public interface LexemeRepository extends ArangoRepository<Lexeme, String> {
    @Query("""
                  // returns all words at a Levenshtein distance maximum of @dist from @word
                  for lexeme in Lexeme
                  //word form to compare levenshtein distance of
                  LET search_form = lexeme.@form
                  // redundant to return same word as input
                  filter search_form not like @word
                  filter levenshtein_distance(@word, search_form) <= @dist
                  
                  // return distinct since multiple lexemes can have same forms
                  return distinct search_form
                  """)
    Iterable<String> getWithLevenshtein(@Param("word") String word, @Param("dist") Integer dist, @Param("form") String form);
    @Query("""
                 for lexeme in Lexeme
                     
                     // Get words matching regex with given form
                     filter lexeme.@form like @regex
                     
                     // return distinct since multiple lexemes can have same forms
                     return distinct lexeme.@form
                  """)
    Iterable<String> getWithRegex(@Param("regex") String regex, @Param("form") String form);
    @Query("""
    //Get meanings of certain type
    //Start from lexemes with same form as input word
    LET start_vertex = (
        for l in Lexeme
        filter l.@form like @word
        return l
    )
    //Find roots of meaning tree : Lexeme <- Entry <- Tree <- Root
    LET meaning_tree_roots = (
    for start in start_vertex
        for v, e, p in 1..3 inbound start GRAPH LexemeMeaningRoot
        //meaning root has no parent
        filter p.vertices[3].parentId == 0
        return p.vertices[3])
    //Traverse meaning tree (parent -> child), returning all meanings of type given
    for root in meaning_tree_roots
        for v, e, p in 0..10 outbound root GRAPH MeaningGraph
        //types: 0) proper meaning, 1) etymology, 2) usage example from literature, 3) comment, 4) diff from parent meaning, 5) compound expression meaning
        filter v.type == @type
        filter v.internalRep != null
        return v.internalRep
""")
    Iterable<String> getMeanings(@Param("word") String word, @Param("type") Integer type, @Param("form") String form);
    @Query("""
// Get Lexemes with relation to input (synonyms, antonyms, diminutives, augmentatives)
//Start: lexemes with same form as input word
LET start_vertices = (
    for l in Lexeme
    filter l.@form like @word
    return l
)
// Get to roots of meaning trees of input : Lexeme <- Entry <- Tree <- Root
LET meaning_roots = (
for start in start_vertices
    for v, e, p in 1..3 inbound start GRAPH LexemeMeaningRoot
    return last(p.vertices)
    )
//meanings with relation to other meaning tree: parent -> child, (if relation exists) child -[relation]> tree
LET relation_meaning = (
    for root in meaning_roots
        for v, e, p in 1..10 outbound root EdgeMeaningMeaning, EdgeRelation
        //types 1) synonym, 2) antonym, 3) diminutive, 4) augmentative
        FILTER last(p.edges).type == @relationType
        //penultimate element - meaning with given relationship
        LET pos = length(p.vertices) - 2
        //Eliminate compound expressions (type 5) containing original word; they have different meanings
        filter p.vertices[pos].type != 5
        return last(p.vertices))
//From tree to lexemes: Tree -> Entry -> Lexeme
for meaning in relation_meaning
    for v, e, p in 1..3 outbound meaning GRAPH LexemeMeaningRoot
    filter last(p.vertices).@form != null
    return distinct last(p.vertices).@form
""")
    Iterable<String> getLexemesWithRelation(@Param("word") String word, @Param("relationType") Integer relationType, @Param("form") String form);
}