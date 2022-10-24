package org.example.codex.forms;

public class DistanceForm {
    String word;
    String form;
    String distanceType;
    Integer neighborCount;

    public DistanceForm(String word, String form, String distanceType, Integer neighborCount) {
        this.word = word;
        this.form = form;
        this.distanceType = distanceType;
        this.neighborCount = neighborCount;
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

    public Integer getNeighborCount() {
        return neighborCount;
    }

    public void setNeighborCount(Integer neighborCount) {
        this.neighborCount = neighborCount;
    }

    public String getDistanceType() {
        return distanceType;
    }

    public void setDistanceType(String distanceType) {
        this.distanceType = distanceType;
    }
}
