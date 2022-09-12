package org.example.codex.forms;

public class RelationForm {
    private String word;
    private Integer relationType; //1 synonym, 2 antonym, 3 diminutive, 4 augmentative
    private Integer collation;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getRelationType() {
        return relationType;
    }

    public void setRelationType(Integer relationType) {
        this.relationType = relationType;
    }

    public Integer getCollation() {
        return collation;
    }

    public void setCollation(Integer collation) {
        this.collation = collation;
    }
}
