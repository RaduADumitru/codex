package org.project.codex.repository;

import com.arangodb.springframework.annotation.Query;
import com.arangodb.springframework.repository.ArangoRepository;
import org.project.codex.responses.EtymologyResponse;
import org.project.codex.model.Lexeme;
import org.project.codex.responses.EdgeResponse;
import org.project.codex.responses.KeyTypeResponse;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QueryRepository extends ArangoRepository<Lexeme, String> {
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
        for v, e, p in 1..3 inbound start EntryLexeme, TreeEntry, MeaningTree
        //meaning root has no parent
        filter p.vertices[3].parentId == 0
        return p.vertices[3])
    //Traverse meaning tree (parent -> child), returning all meanings of type given
    for root in meaning_tree_roots
        for v, e, p in 0..10 outbound root MeaningMeaning
        //types: 0) proper meaning, 1) etymology, 2) usage example from literature, 3) comment, 4) diff from parent meaning, 5) compound expression meaning
        filter v.type == @type
        filter v.internalRep != null
        return v.internalRep
""")
    Iterable<String> getMeanings(@Param("word") String word, @Param("type") Integer type, @Param("form") String form);
    @Query("""
    //insert array of etymologies into each lexeme
    for l in Lexeme
    //Find roots of meaning tree : Lexeme <- Entry <- Tree <- Root
    LET meaning_tree_roots = (
        for v, e, p in 1..3 inbound l EntryLexeme, TreeEntry, MeaningTree
        //meaning root has no parent
        filter p.vertices[3].parentId == 0
        return p.vertices[3]
        )
    //Traverse meaning tree (parent -> child), returning all meanings of type given
    let meanings = (    
    for root in meaning_tree_roots
        for v, e, p in 0..10 outbound root MeaningMeaning
        //types: 0) proper meaning, 1) etymology, 2) usage example from literature, 3) comment, 4) diff from parent meaning, 5) compound expression meaning
        return distinct v
        )
    let etymologies = (
    for m in meanings
    filter m.type == 1
    return m
    )
    let etymology_array = (
    for etymology in etymologies
    for v, e, p in 1..1 outbound etymology ObjectTag
    return {originalWord: etymology.internalRep, tag: p.vertices[1].value}
    )
    update { _key: l._key, etymologies: etymology_array} in Lexeme
""")
    void insertEtymologies();
    @Query("""
let page = (
   for l in Lexeme
   sort l._key asc
   limit @skip, @pagesize
   return l
  )
    //insert array of etymologies into each lexeme
    for l in page
    LET meaning_tree_roots = (
        for v, e, p in 1..3 inbound l EntryLexeme, TreeEntry, MeaningTree
        filter p.vertices[3].parentId == 0
        return p.vertices[3]
        )
    //Traverse meaning tree (parent -> child), returning all meanings of type given
    let meanings = (
    for root in meaning_tree_roots
        for v, e, p in 0..10 outbound root MeaningMeaning
        //type 1: etymology
        return distinct v
        )
    let etymologies = (
    for m in meanings
    filter m.type == 1
    return m
    )
    let etymology_array = (
    for etymology in etymologies
    for v, e, p in 1..1 outbound etymology ObjectTag
    return {originalWord: etymology.internalRep, tag: p.vertices[1].value}
    )
    update { _key: l._key, etymologies: etymology_array} in Lexeme
""")
    void insertEtymologiesWithPagination(@Param("skip") Integer skip, @Param("pagesize") Integer pagesize);
    @Query("""
    //insert array of meanings into each lexeme
    for l in Lexeme
    //Find roots of meaning tree : Lexeme <- Entry <- Tree <- Root
    LET meaning_tree_roots = (
        for v, e, p in 1..3 inbound l EntryLexeme, TreeEntry, MeaningTree
        //meaning root has no parent
        filter p.vertices[3].parentId == 0
        return p.vertices[3])
    //Traverse meaning tree (parent -> child), returning all meanings of type given
    let meanings = (    for root in meaning_tree_roots
        for v, e, p in 0..10 outbound root MeaningMeaning
        //types: 0) proper meaning, 1) etymology, 2) usage example from literature, 3) comment, 4) diff from parent meaning, 5) compound expression meaning
        filter v.internalRep != null and v.internalRep != ""
        filter v.type == 0 or v.type == 5
        return distinct v.internalRep)
    update {_key: l._key, meanings: meanings} in Lexeme
""")
    void insertMeanings();
    @Query("""
   let page = (
   for l in Lexeme
   sort l._key asc
   limit @skip, @pagesize
   return l
   )
    //insert array of meanings into each lexeme
    for l in page
    //Find roots of meaning tree : Lexeme <- Entry <- Tree <- Root
    LET meaning_tree_roots = (
        for v, e, p in 1..3 inbound l EntryLexeme, TreeEntry, MeaningTree
        //meaning root has no parent
        filter p.vertices[3].parentId == 0
        return p.vertices[3])
    //Traverse meaning tree (parent -> child), returning all meanings of type given
    let meanings = (    for root in meaning_tree_roots
        for v, e, p in 0..10 outbound root MeaningMeaning
        //types: 0) proper meaning, 1) etymology, 2) usage example from literature, 3) comment, 4) diff from parent meaning, 5) compound expression meaning
        filter v.internalRep != null and v.internalRep != ""
        filter v.type == 0 or v.type == 5
        return distinct v.internalRep)
    update {_key: l._key, meanings: meanings} in Lexeme
""")
    void insertMeaningsWithPagination(@Param("skip") Integer skip, @Param("pagesize") Integer pagesize);
    @Query("""
    //insert array of usage examples into each lexeme
    for l in Lexeme
    //Find roots of meaning tree : Lexeme <- Entry <- Tree <- Root
    LET meaning_tree_roots = (
        for v, e, p in 1..3 inbound l EntryLexeme, TreeEntry, MeaningTree
        //meaning root has no parent
        filter p.vertices[3].parentId == 0
        return p.vertices[3])
    //Traverse meaning tree (parent -> child), returning all meanings of type given
    let examples = (    for root in meaning_tree_roots
        for v, e, p in 0..10 outbound root MeaningMeaning
        //types: 0) proper meaning, 1) etymology, 2) usage example from literature, 3) comment, 4) diff from parent meaning, 5) compound expression meaning
        filter v.internalRep != null and v.internalRep != ""
        filter v.type == 2
        return distinct v.internalRep)
    update {_key: l._key, usageExamples: examples} in Lexeme
""")
    void insertUsageExamples();
    @Query("""
       let page = (
   for l in Lexeme
   sort l._key asc
   limit @skip, @pagesize
   return l)
        //insert array of usage examples into each lexeme
    for l in page
    //Find roots of meaning tree : Lexeme <- Entry <- Tree <- Root
    LET meaning_tree_roots = (
        for v, e, p in 1..3 inbound l EntryLexeme, TreeEntry, MeaningTree
        //meaning root has no parent
        filter p.vertices[3].parentId == 0
        return p.vertices[3])
    //Traverse meaning tree (parent -> child), returning all meanings of type given
    let examples = (    
    for root in meaning_tree_roots
        for v, e, p in 0..10 outbound root MeaningMeaning
        //types: 0) proper meaning, 1) etymology, 2) usage example from literature, 3) comment, 4) diff from parent meaning, 5) compound expression meaning
        filter v.internalRep != null and v.internalRep != ""
        filter v.type == 2
        return distinct v.internalRep
        )
    update {_key: l._key, usageExamples: examples} in Lexeme
""")
    void insertUsageExamplesWithPagination(@Param("skip") Integer skip, @Param("pagesize") Integer pagesize);
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
    for v, e, p in 1..3 inbound start EntryLexeme, TreeEntry, MeaningTree
    return last(p.vertices)
    )
//meanings with relation to other meaning tree: parent -> child, (if relation exists) child -[relation]> tree
LET relation_meaning = (
    for root in meaning_roots
        for v, e, p in 1..10 outbound root MeaningMeaning, Relation
        //types 1) synonym, 2) antonym, 3) diminutive, 4) augmentative
        FILTER last(p.edges).type == @relationType
        //penultimate element - meaning with given relationship
        LET pos = length(p.vertices) - 2
        //Eliminate compound expressions (type 5) containing original word; they have different meanings
        filter p.vertices[pos].type != 5
        return last(p.vertices))
//From tree to lexemes: Tree -> Entry -> Lexeme
for meaning in relation_meaning
    for v, e, p in 1..3 outbound meaning MeaningTree,  TreeEntry, EntryLexeme
    filter last(p.vertices).@form != null
    return distinct last(p.vertices).@form
""")
    Iterable<String> getLexemesWithRelation(@Param("word") String word, @Param("relationType") Integer relationType, @Param("form") String form);
    @Query("""
//Similar to above, but get full lexeme and insert into Relation
//replace numeric codes with corresponding relation
let translatemap = {"1": "synonym", "2": "antonym", "3": "diminutive", "4": "augmentative"}
for l in Lexeme
// Get to roots of meaning trees of input : Lexeme <- Entry <- Tree <- Root
LET meaning_roots = (
    for v, e, p in 3..3 inbound l EntryLexeme, TreeEntry, MeaningTree
    return last(p.vertices)
    )
//meanings with relation to other meaning tree: parent -> child, (if relation exists) child -[relation]> tree
LET relation_tree_pair = (
    for root in meaning_roots
        for v, e, p in 1..10 outbound root MeaningMeaning, Relation
//        //types 1) synonym, 2) antonym, 3) diminutive, 4) augmentative
        FILTER last(p.edges).type != null
        LET pos = length(p.vertices) - 2
        //Eliminate compound expressions (type 5) containing original word; they have different meanings
        filter p.vertices[pos].type != 5
        collect tree = last(p.vertices), relation_type = last(p.edges).type
        return {tree: tree, relationType: relation_type})
for pair in relation_tree_pair
    for v, e, p in 2..2 outbound pair.tree TreeEntry, EntryLexeme
    let type_string = to_string(pair.relationType)
    insert {_from: l._id, _to: last(p.vertices)._id, type: translate(type_string, translatemap)} into RelationTemp
""")
    void createRelationTemp();
    @Query("""
let translatemap = {"1": "synonym", "2": "antonym", "3": "diminutive", "4": "augmentative"}
let page = (
   for l in Lexeme
   sort l._key asc
   limit @skip, @pagesize
   return l)
        //insert array of usage examples into each lexeme
for l in page
// Get to roots of meaning trees of input : Lexeme <- Entry <- Tree <- Root
LET meaning_roots = (
    for v, e, p in 3..3 inbound l EntryLexeme, TreeEntry, MeaningTree
    return last(p.vertices)
    )
//meanings with relation to other meaning tree: parent -> child, (if relation exists) child -[relation]> tree
LET relation_tree_pair = (
    for root in meaning_roots
        for v, e, p in 1..10 outbound root MeaningMeaning, Relation
//        //types 1) synonym, 2) antonym, 3) diminutive, 4) augmentative
        FILTER last(p.edges).type != null
        LET pos = length(p.vertices) - 2
        //Eliminate compound expressions (type 5) containing original word; they have different meanings
        filter p.vertices[pos].type != 5
        collect tree = last(p.vertices), relation_type = last(p.edges).type
        return {tree: tree, relationType: relation_type})
for pair in relation_tree_pair
    for v, e, p in 2..2 outbound pair.tree TreeEntry, EntryLexeme
    let type_string = to_string(pair.relationType)
    insert {_from: l._id, _to: last(p.vertices)._id, type: translate(type_string, translatemap)} into RelationTemp
""")
    void createRelationTempWithPagination(@Param("skip") Integer skip, @Param("pagesize") Integer pagesize);

    @Query("""
for col in collections()
filter not starts_with(col.name, "_") //do not consider system collections
return col.name
""")
    List<String> getCollections();
    @Query("""
let col_keys_type = (
for obj in @@col
for key in keys(obj)
return {"key": key, "type": typename(obj[key])}
)
let pairs = (
for pair in col_keys_type
return distinct pair
)
for pair in pairs
sort pair.key, pair.type
return pair
""")
    List<KeyTypeResponse> getKeyTypes(@Param("@col") String collection);
    @Query("""
//Get a document from collection, and see if it has _from attribute, exclusive to edges
for obj in @@col
limit 1
let atts = attributes(obj)
return "_from" in atts
""")
    boolean isEdgeCollection(@Param("@col") String collection);
    @Query("""
for obj in @@col
// _from and _to are in format Collection/id : split string by /, collection name is first element
let from_collection = split(obj._from, "/")[0]
let to_collection = split(obj._to, "/")[0]
return distinct {"from": from_collection, "to": to_collection}
""")
    List<EdgeResponse> getEdgeRelations(@Param("@col") String collection);
    @Query("""
let all_matching_lexemes = (
for l in Lexeme
    let search_form = l.@form
    // avoid returning same string
    filter search_form != @word
    let distance = levenshtein_distance(search_form, @word)
    sort distance asc
    return distinct search_form
)
for l in all_matching_lexemes
    limit @neighborcount
    return l
""")
    List<String> getKnnLevenshtein(@Param("word") String word, @Param("form") String form, @Param("neighborcount") Integer neighborcount);
    @Query("""
let all_matching_lexemes = (
for l in Lexeme
    let search_form = l.@form
    
    // hamming distance allows only character substitutions, for strings of same length
    filter length(search_form) == length(@word)
    
    // avoid returning same string
    filter search_form != @word
    //in this case hamming distance will be equal to levenshtein distance
    let distance = levenshtein_distance(search_form, @word)
    sort distance asc
    return distinct search_form
)
for l in all_matching_lexemes
    limit @neighborcount
    return l
""")
    List<String> getKnnHamming(@Param("word") String word, @Param("form") String form, @Param("neighborcount") Integer neighborcount);
    @Query("""
let all_matching_lexemes = (
for l in Lexeme
    let search_form = l.@form
    // LCS distance allows only character insertions or deletions, so only valid when one of the strings contains the other
    filter contains(search_form, @word) or contains(@word, search_form)
    // avoid returning same string
    filter search_form != @word
    //in this case LCS distance will be equal to levenshtein distance
    let distance = levenshtein_distance(search_form, @word)
    sort distance asc
    return distinct search_form
)
for l in all_matching_lexemes
    limit @neighborcount
    return l
""")
    List<String> getKnnLCS(@Param("word") String word, @Param("form") String form, @Param("neighborcount") Integer neighborcount);
    @Query("""
let all_matching_lexemes = (
for l in Lexeme
    let search_form = l.@form
    // avoid returning same string
    filter search_form != @word
    let ngramsize = @ngramsize
//    let distance = levenshtein_distance(l.formUtf8General, "anaaremere")
    let distance = ngram_similarity(@word, search_form, ngramsize)
    sort distance desc
    return distinct search_form
)
for l in all_matching_lexemes
    limit @neighborcount
    return l
""")
    List<String> getKnnNgramSimilarity(@Param("word") String word, @Param("form") String form, @Param("ngramsize") Integer ngramSize, @Param("neighborcount") Integer neighborcount);
    @Query("""
let all_matching_lexemes = (
for l in Lexeme
    let search_form = l.@form
    // avoid returning same string
    filter search_form != @word
    let ngramsize = @ngramsize
//    let distance = levenshtein_distance(l.formUtf8General, "anaaremere")
    let distance = ngram_positional_similarity(@word, search_form, ngramsize)
    sort distance desc
    return distinct search_form
)
for l in all_matching_lexemes
    limit @neighborcount
    return l
""")
    List<String> getKnnNgramPositionalSimilarity(@Param("word") String word, @Param("form") String form, @Param("ngramsize") Integer ngramSize, @Param("neighborcount") Integer neighborcount);
    @Query("""
for obj in @@attributecollection
    //bug with standard concat() function, so use concat_separator with empty separator as workaround
    
    let fromattributevalue = obj.@fromattribute
    let fromvalue = concat_separator("", @fromcollection, "/", fromattributevalue)
    
    let toattributevalue = obj.@toattribute
    let tovalue = concat_separator("", @tocollection, "/", toattributevalue)
    
    insert { _from: fromvalue, _to: tovalue } into @@generatedcollection
    """)
    void insertIntoGeneratedCollection(@Param("fromcollection") String fromCollection, @Param("tocollection") String toCollection, @Param("@attributecollection") String attributeCollection, @Param("@generatedcollection") String generatedCollection, @Param("fromattribute") String fromAttribute, @Param("toattribute") String toAttribute);
    @Query("""
let page = (
for obj in @@attributecollection
sort obj._key asc
//get next @pagesize elements starting from @skip
limit @skip, @pagesize
return obj
)
for obj in page
    //bug with standard concat() function, so use concat_separator with empty separator as workaround
    let fromattributevalue = obj.@fromattribute
    let fromvalue = concat_separator("", @fromcollection, "/", fromattributevalue)
    let toattributevalue = obj.@toattribute
    let tovalue = concat_separator("", @tocollection, "/", toattributevalue)
    insert { _from: fromvalue, _to: tovalue } into @@generatedcollection
    """)
    void insertIntoGeneratedCollectionWithPagination(@Param("fromcollection") String fromCollection, @Param("tocollection") String toCollection, @Param("@attributecollection") String attributeCollection, @Param("@generatedcollection") String generatedCollection, @Param("fromattribute") String fromAttribute, @Param("toattribute") String toAttribute, @Param("skip") Integer skip, @Param("pagesize") Integer pagesize);
    @Query("""
for l in Lexeme
//language codes: ISO 639-1
update {_key: l._key, language: "ro"} in Lexeme
""")
    void setRomanianLanguage();
    @Query("""
for obj in @@collection
replace obj with unset(obj, @field) in @@collection
""")
    void unsetAttribute(@Param("@collection") String collection, @Param("field") String field);
    @Query("""
let start_lexemes = (
for l in Lexeme
filter l.@form == @word
return l)
for l in start_lexemes
//words with given relationship are neighbors as described by Relation edge collection
for v, e, p in 1..1 outbound l Relation
    filter p.edges[0].type == @type
    return distinct p.vertices[1].@form
""")
    List<String> optimizedGetLexemesWithRelation(@Param("word") String word, @Param("form") String form, @Param("type") String type);
    @Query("""
let start_lexemes = (
for l in Lexeme
filter l.@form == @word
return l)
let result = (
for l in start_lexemes
return distinct l.meanings
)
//returns array of arrays, which will be transformed into single array by flatten()
let result_array = unique(flatten(result))
for elem in result_array
return elem
""")
    List<String> optimizedGetMeanings(@Param("word") String word, @Param("form") String form);
    @Query("""
let start_lexemes = (
for l in Lexeme
filter l.@form == @word
return l)
let result = (
for l in start_lexemes
return distinct l.usageExamples
)
//returns array of arrays, which will be transformed into single array by flatten()
let result_array = unique(flatten(result))
for elem in result_array
return elem
""")
    List<String> optimizedGetUsageExamples(@Param("word") String word, @Param("form") String form);
    @Query("""
let start_lexemes = (
for l in Lexeme
filter l.@form == @word
return l)
let result = (
for l in start_lexemes
return distinct l.etymologies
)
let result_array = unique(flatten(result))
for elem in result_array
return elem
""")
    List<EtymologyResponse> optimizedGetEtymologies(@Param("word") String word, @Param("form") String form);
    @Query("""
return count(@@col)
""")
    Integer getCollectionDocumentCount(@Param("@col") String collection);
}
