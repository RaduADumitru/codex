package org.project.codex.forms;

public class LevenshteinForm {
    private String word;
    private Integer distance;
    private String wordform;

    public LevenshteinForm(String word, Integer distance, String wordform) {
        this.word = word;
        this.distance = distance;
        this.wordform = wordform;
    }

    public LevenshteinForm() {
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public String getWordform() {
        return wordform;
    }

    public void setWordform(String wordform) {
        this.wordform = wordform;
    }
}
