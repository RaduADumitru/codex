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
    LET start_vertex = (
        for l in Lexeme
        filter l.formUtf8General like @word
        return l
    )
    LET meaning_tree_roots = (
    for start in start_vertex
        for v, e, p in 1..3 inbound start GRAPH LexemeMeaningRoot
        filter p.vertices[3].parentId == 0
        return p.vertices[3])
    for root in meaning_tree_roots
        for v, e, p in 0..10 outbound root GRAPH MeaningGraph
        filter v.type == @type
        filter v.internalRep != null
        return v.internalRep
""")
    Iterable<String> getMeanings(@Param("word") String word, @Param("type") Integer type);
    @Query("""
// Get Synonyms of lexeme
//start from vertices with same form as word parameter
//1) Get all words that have selected word as synonym in meanings
LET start_vertices = (
    for l in Lexeme
    filter l.formNoAccent like "alb"
    return l
)
//Relationship edge connects tree to meanings with desired relationship, those meanings will be had by the words we are looking for
//path: Lexeme <- Entry <- Tree <[Relationship with type]- Meaning <- MeaningTreeRoot <- Tree <- Entry <- Lexeme\s
LET relation_meanings = (
for start in start_vertices
    for v, e, p in 1..3 inbound start GRAPH Synonyms
    filter p.edges[2].type == 1
        return last(p.vertices)
)
//Get to roots of meaning trees: parent -> child: roots will be end vertices traversing inward
LET meaning_roots = (
    for meaning in relation_meanings
        for v, e, p in 1..10 inbound meaning GRAPH MeaningGraph
        //Get only paths that have ended
        filter length(for vv in inbound v Graph MeaningGraph limit 1 return 1) == 0
            return last(p.vertices)
)
//go from roots to corresponding lexemes: Meaning -> Tree -> Entry -> Lexeme
LET end_lexemes_1 = (
for root in meaning_roots
        for v, e, p in 1..3 outbound root GRAPH LexemeMeaningRoot
        //Get only paths that have ended
        filter length(for vv in outbound v Graph LexemeMeaningRoot limit 1 return 1) == 0
            return last(p.vertices)
        )
        //2) Get all synonyms of selected word in article
        LET meaning_roots_2 = (
        for start in start_vertices
            for v, e, p in 1..3 inbound start GRAPH LexemeMeaningRoot
            return last(p.vertices)
            )
        LET new_meaning = (
            for root in meaning_roots_2
                for v, e, p in 1..10 outbound root EdgeMeaningMeaning, EdgeRelation
                FILTER last(p.edges).type == 1
                //Eliminate synonyms of compound expressions containing original word
                LET pos = length(p.vertices) - 2
                filter p.vertices[pos].type != 5
                return last(p.vertices)
        )
        let end_lexemes_2 = (
        for meaning in new_meaning
            for v, e, p in 1..3 outbound meaning GRAPH LexemeMeaningRoot
            return last(p.vertices)
        )
        LET forms_1 = (
        for l in end_lexemes_1
        filter l.formUtf8General != null
        return l.formUtf8General
        )
        LET forms_2 = (
        for l in end_lexemes_2
        filter l.formUtf8General != null
        return l.formUtf8General
        )
        RETURN UNION_DISTINCT(forms_1, forms_2)
""")
    Iterable<String> getLexemesWithRelation(@Param("word") String word, @Param("relationType") Integer type, @Param("form") String form);
}