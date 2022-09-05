package org.example.codex.forms;

public class MeaningsForm {
    private String word;
    private Integer type;

    public MeaningsForm(String word, Integer type) {
        this.word = word;
        this.type = type;
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
