package org.project.codex.forms;

public class MeaningsForm {
    private String word;
    private String meaningtype;
    private String wordform;

    public MeaningsForm(String word, String meaningtype, String wordform) {
        this.word = word;
        this.meaningtype = meaningtype;
        this.wordform = wordform;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaningtype() {
        return meaningtype;
    }

    public void setMeaningtype(String meaningtype) {
        this.meaningtype = meaningtype;
    }

    public String getWordform() {
        return wordform;
    }

    public void setWordform(String wordform) {
        this.wordform = wordform;
    }
}
