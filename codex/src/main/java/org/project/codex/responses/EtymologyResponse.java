package org.project.codex.responses;

public class EtymologyResponse {
    private String originalWord;
    private String tag;

    public EtymologyResponse(String originalWord, String tag) {
        this.originalWord = originalWord;
        this.tag = tag;
    }

    public EtymologyResponse() {
    }

    public String getOriginalWord() {
        return originalWord;
    }

    public void setOriginalWord(String originalWord) {
        this.originalWord = originalWord;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
