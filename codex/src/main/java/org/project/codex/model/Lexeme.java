package org.project.codex.model;

import com.arangodb.springframework.annotation.ArangoId;
import com.arangodb.springframework.annotation.Document;
import org.springframework.data.annotation.Id;

/* Documentation: https://github.com/dexonline/dexonline/wiki/Database-schema%3A-the-Lexeme-table*/
@Document("Lexeme")
public class Lexeme {
    //Unused code below

    @Id // db document field: _key
    private String id;

    @ArangoId // db document field: _id
    private String arangoId;

/* Base form of the word, including an apostrophe (') for the accent.
This makes it impractical for searches.*/
    private String form;

    /* These two have equal values, obtained from form by removing the accent.
    The fields have different collates (utf8_romanian_ci and utf8_general_ci, respectively).
    The former is used for searches including diacritics,
    the latter is used for searches without diacritics,
    because in utf8_general_ci ă = â = a, ș = s and so on.*/
    private String formNoAccent;
    private String formUtf8General;

    /* the reverse of formNoAccent.
    Only used in certain admin tools where we need suffix matching.*/
    private String reverse;

    /* Generally used to distinguish among homonyms.*/
    private String description;

    /* If true, indicates that this lexeme does not need an accent.
    Otherwise, a report on the admin page shows all the lexemes
    for which form does not include an accent.*/
    private Boolean noAccent;

    /* True if the combination of form and noAccent is consistent.
    This field is automatically set by the system and is outside the user's control.*/
    private Boolean consistentAccent;

    /* The declension model for this lexeme.
    Note that modelType may be canonical (A, F, M etc.)
    or non-canonical (AN, MF, VT etc.).
    Specifically, in order to find the Model record for a lexeme,
    you should first look up the canonical model type using the ModelType table.*/
    private String modelType;
    private Integer modelNumber;

    /* A set of case-sensitive letters
    (documented here: https://wiki.dexonline.ro/wiki/Ghid_pentru_structurare#Pagina_de_editare_a_lexemului)
    indicating that this lexeme only generates a subset of the allowed word forms for the lexeme's model.
    To see which restrictions allow which forms, use the ConstraintMap table.
    For example, the verb a ploua (to rain) only allows third person singular form,
    hence the restriction I.*/
    private String restriction;

    /* True for compound lexemes like Steaua Polară (The North Star). See the Fragment table for details.*/
    private Boolean compound;

    /* True for lexemes whose model definition has changed.
    There is an admin report listing lexemes with stale paradigms.
    This system allows us to edit models with tens of thousands of lexemes
    without having to regenerate all the paradigms on the spot.*/
    private Boolean staleParadigm;

    /* A publicly visible explanation for how the declension model was chosen
    (example: https://dexonline.ro/definitie/azbociment/paradigma).*/
    private String notes;

    /* Indicates that forms of this lexeme that begin with î can also exist without the î.
    For the verb a fi (to be), the popular forms (eu) îs and (el) îi become -s and -i.*/
    private Boolean hasApheresis;

    /* Indicates that certain forms of this lexeme may lose their final letter,
    for example copilu- for copilul (the child).
    This omission is called an apocope.*/
    private Boolean hasApocope;

    /* Relative frequency of the lexeme, normalized from 0 to 1.
    This was computed a long time ago, using dexonline's definition bodies,
    and is not very reliable.*/
    private Float frequency;

    /* Comma-separated list of the lexeme's hyphenations. Can be empty.*/
    private String hyphenations;

    /* Comma-separated list of the lexeme's pronunciations. Can be empty.*/
    private String pronunciations;

    /* True for stop words which should be excluded from full-text searches.
    These include prepositions, articles and other secondary parts of speech.
    This greatly reduces the size of the full-text index table.*/
    private Boolean stopWord;

    /* undocumented */
    private String number;

    public Lexeme(String id, String arangoId, String form, String formNoAccent, String formUtf8General, String reverse, String description, Boolean noAccent, Boolean consistentAccent, String modelType, Integer modelNumber, String restriction, Boolean compound, Boolean staleParadigm, String notes, Boolean hasApheresis, Boolean hasApocope, Float frequency, String hyphenations, String pronunciations, Boolean stopWord, String number) {
        this.id = id;
        this.arangoId = arangoId;
        this.form = form;
        this.formNoAccent = formNoAccent;
        this.formUtf8General = formUtf8General;
        this.reverse = reverse;
        this.description = description;
        this.noAccent = noAccent;
        this.consistentAccent = consistentAccent;
        this.modelType = modelType;
        this.modelNumber = modelNumber;
        this.restriction = restriction;
        this.compound = compound;
        this.staleParadigm = staleParadigm;
        this.notes = notes;
        this.hasApheresis = hasApheresis;
        this.hasApocope = hasApocope;
        this.frequency = frequency;
        this.hyphenations = hyphenations;
        this.pronunciations = pronunciations;
        this.stopWord = stopWord;
        this.number = number;
    }

    public Lexeme() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArangoId() {
        return arangoId;
    }

    public void setArangoId(String arangoId) {
        this.arangoId = arangoId;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getFormNoAccent() {
        return formNoAccent;
    }

    public void setFormNoAccent(String formNoAccent) {
        this.formNoAccent = formNoAccent;
    }

    public String getFormUtf8General() {
        return formUtf8General;
    }

    public void setFormUtf8General(String formUtf8General) {
        this.formUtf8General = formUtf8General;
    }

    public String getReverse() {
        return reverse;
    }

    public void setReverse(String reverse) {
        this.reverse = reverse;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getNoAccent() {
        return noAccent;
    }

    public void setNoAccent(Boolean noAccent) {
        this.noAccent = noAccent;
    }

    public Boolean getConsistentAccent() {
        return consistentAccent;
    }

    public void setConsistentAccent(Boolean consistentAccent) {
        this.consistentAccent = consistentAccent;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public Integer getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(Integer modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getRestriction() {
        return restriction;
    }

    public void setRestriction(String restriction) {
        this.restriction = restriction;
    }

    public Boolean getCompound() {
        return compound;
    }

    public void setCompound(Boolean compound) {
        this.compound = compound;
    }

    public Boolean getStaleParadigm() {
        return staleParadigm;
    }

    public void setStaleParadigm(Boolean staleParadigm) {
        this.staleParadigm = staleParadigm;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getHasApheresis() {
        return hasApheresis;
    }

    public void setHasApheresis(Boolean hasApheresis) {
        this.hasApheresis = hasApheresis;
    }

    public Boolean getHasApocope() {
        return hasApocope;
    }

    public void setHasApocope(Boolean hasApocope) {
        this.hasApocope = hasApocope;
    }

    public Float getFrequency() {
        return frequency;
    }

    public void setFrequency(Float frequency) {
        this.frequency = frequency;
    }

    public String getHyphenations() {
        return hyphenations;
    }

    public void setHyphenations(String hyphenations) {
        this.hyphenations = hyphenations;
    }

    public String getPronunciations() {
        return pronunciations;
    }

    public void setPronunciations(String pronunciations) {
        this.pronunciations = pronunciations;
    }

    public Boolean getStopWord() {
        return stopWord;
    }

    public void setStopWord(Boolean stopWord) {
        this.stopWord = stopWord;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "Lexeme{" +
                "id='" + id + '\'' +
                ", arangoId='" + arangoId + '\'' +
                ", form='" + form + '\'' +
                ", formNoAccent='" + formNoAccent + '\'' +
                ", formUtf8General='" + formUtf8General + '\'' +
                ", reverse='" + reverse + '\'' +
                ", description='" + description + '\'' +
                ", noAccent=" + noAccent +
                ", consistentAccent=" + consistentAccent +
                ", modelType='" + modelType + '\'' +
                ", modelNumber=" + modelNumber +
                ", restriction='" + restriction + '\'' +
                ", compound=" + compound +
                ", staleParadigm=" + staleParadigm +
                ", notes='" + notes + '\'' +
                ", hasApheresis=" + hasApheresis +
                ", hasApocope=" + hasApocope +
                ", frequency=" + frequency +
                ", hyphenations='" + hyphenations + '\'' +
                ", pronunciations='" + pronunciations + '\'' +
                ", stopWord=" + stopWord +
                ", number='" + number + '\'' +
                '}';
    }
}