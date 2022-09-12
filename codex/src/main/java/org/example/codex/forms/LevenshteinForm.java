package org.example.codex.forms;

public class LevenshteinForm {
    private String word;
    private Integer dist;
    private Integer collation;

    public Integer getCollation() {
        return collation;
    }

    public void setCollation(Integer collation) {
        this.collation = collation;
    }

    public LevenshteinForm(String word, Integer dist) {
        this.word = word;
        this.dist = dist;
    }

    public LevenshteinForm(String word, Integer dist, Integer collation) {
        this.word = word;
        this.dist = dist;
        this.collation = collation;
    }

    public LevenshteinForm() {
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getDist() {
        return dist;
    }

    public void setDist(Integer dist) {
        this.dist = dist;
    }
}
