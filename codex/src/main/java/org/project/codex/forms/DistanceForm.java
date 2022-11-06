package org.project.codex.forms;

public class DistanceForm {
    private String word;
    private String wordform;
    private String distancetype;
    private Integer neighborcount;

    public DistanceForm(String word, String wordform, String distancetype, Integer neighborcount) {
        this.word = word;
        this.wordform = wordform;
        this.distancetype = distancetype;
        this.neighborcount = neighborcount;
    }

    public DistanceForm() {
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

    public Integer getNeighborcount() {
        return neighborcount;
    }

    public void setNeighborcount(Integer neighborcount) {
        this.neighborcount = neighborcount;
    }

    public String getDistancetype() {
        return distancetype;
    }

    public void setDistancetype(String distancetype) {
        this.distancetype = distancetype;
    }

    @Override
    public String toString() {
        return "DistanceForm{" +
                "word='" + word + '\'' +
                ", form='" + wordform + '\'' +
                ", distanceType='" + distancetype + '\'' +
                ", neighborCount=" + neighborcount +
                '}';
    }
}
