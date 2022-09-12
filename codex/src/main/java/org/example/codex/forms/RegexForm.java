package org.example.codex.forms;

public class RegexForm {
    private String regex;
    private Integer collation;

    public RegexForm(String regex) {
        this.regex = regex;
    }

    public RegexForm(String regex, Integer collation) {
        this.regex = regex;
        this.collation = collation;
    }

    public Integer getCollation() {
        return collation;
    }

    public void setCollation(Integer collation) {
        this.collation = collation;
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
