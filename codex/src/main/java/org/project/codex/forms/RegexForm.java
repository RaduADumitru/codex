package org.project.codex.forms;

public class RegexForm {
    private String regex;
    private String wordform;

    public RegexForm(String regex, String wordform) {
        this.regex = regex;
        this.wordform = wordform;
    }

    public String getWordform() {
        return wordform;
    }

    public void setWordform(String wordform) {
        this.wordform = wordform;
    }

    public RegexForm() {
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
