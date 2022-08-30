package org.example.codex.forms;

public class RegexForm {
    private String regex;

    public RegexForm(String regex) {
        this.regex = regex;
    }

    public RegexForm() {
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public String toString() {
        return "RegexForm{" +
                "regex='" + regex + '\'' +
                '}';
    }
}
