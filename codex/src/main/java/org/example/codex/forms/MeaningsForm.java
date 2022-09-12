package org.example.codex.forms;

public class MeaningsForm {
    private String word;
    private Integer type;
    private Integer collation;

    public MeaningsForm(String word, Integer type) {
        this.word = word;
        this.type = type;
    }

    public Integer getCollation() {
        return collation;
    }

    public void setCollation(Integer collation) {
        this.collation = collation;
    }

    public MeaningsForm(String word, Integer type, Integer collation) {
        this.word = word;
        this.type = type;
        this.collation = collation;
    }

    public MeaningsForm() {
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
