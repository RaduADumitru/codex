package org.example.codex.forms;

public class NgramForm {
    String word;
    String form;
    Integer nGramSize;
    String distanceType;
    Integer neighborCount;

    public NgramForm(String word, String form, Integer nGramSize, String distanceType, Integer neighborCount) {
        this.word = word;
        this.form = form;
        this.nGramSize = nGramSize;
        this.distanceType = distanceType;
        this.neighborCount = neighborCount;
    }

    public String getDistanceType() {
        return distanceType;
    }

    public void setDistanceType(String distanceType) {
        this.distanceType = distanceType;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public Integer getnGramSize() {
        return nGramSize;
    }

    public void setnGramSize(Integer nGramSize) {
        this.nGramSize = nGramSize;
    }

    public Integer getNeighborCount() {
        return neighborCount;
    }

    public void setNeighborCount(Integer neighborCount) {
        this.neighborCount = neighborCount;
    }
}
