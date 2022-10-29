package org.example.codex.enums;
public enum LexemeForm {
    ACCENT("form"),
    NOACCENT("formNoAccent"),
    UTF8("formUtf8General");
    final String field;

    LexemeForm(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
