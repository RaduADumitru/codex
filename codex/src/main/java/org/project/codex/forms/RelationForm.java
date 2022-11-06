package org.project.codex.forms;

public class RelationForm {
    private String word;
    private String relationtype; //1 synonym, 2 antonym, 3 diminutive, 4 augmentative
    private String wordform;

    public RelationForm(String word, String relationtype, String wordform) {
        this.word = word;
        this.relationtype = relationtype;
        this.wordform = wordform;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getRelationtype() {
        return relationtype;
    }

    public void setRelationtype(String relationtype) {
        this.relationtype = relationtype;
    }

    public String getWordform() {
        return wordform;
    }

    public void setWordform(String wordform) {
        this.wordform = wordform;
    }
}
