package org.project.codex.forms;

public class NgramForm {
    private String word;
    private String wordform;
    private Integer ngramsize;
    private String distancetype;
    private Integer neighborcount;

    public NgramForm(String word, String wordform, Integer ngramsize, String distancetype, Integer neighborcount) {
        this.word = word;
        this.wordform = wordform;
        this.ngramsize = ngramsize;
        this.distancetype = distancetype;
        this.neighborcount = neighborcount;
    }

    public NgramForm() {
    }

    public String getDistancetype() {
        return distancetype;
    }

    public void setDistancetype(String distancetype) {
        this.distancetype = distancetype;
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

    public Integer getNgramsize() {
        return ngramsize;
    }

    public void setNgramsize(Integer ngramsize) {
        this.ngramsize = ngramsize;
    }

    public Integer getNeighborcount() {
        return neighborcount;
    }

    public void setNeighborcount(Integer neighborcount) {
        this.neighborcount = neighborcount;
    }

    @Override
    public String toString() {
        return "NgramForm{" +
                "word='" + word + '\'' +
                ", form='" + wordform + '\'' +
                ", nGramSize=" + ngramsize +
                ", distanceType='" + distancetype + '\'' +
                ", neighborCount=" + neighborcount +
                '}';
    }
}
