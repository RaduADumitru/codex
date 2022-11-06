package org.project.codex.forms;

public class OptimizedMeaningForm {
    String word;
    String wordform;

    public OptimizedMeaningForm(String word, String wordform) {
        this.word = word;
        this.wordform = wordform;
    }

    public OptimizedMeaningForm() {
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWordform() {
        return wordform;
    }

    public void setWordform(String wordform) {
        this.wordform = wordform;
    }
}
