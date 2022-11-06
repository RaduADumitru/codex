package org.project.codex.enums;
public enum LexemeForm {
    ACCENT("form"),
    NOACCENT("formNoAccent");
    //corresponding field in database
    final String field;

    LexemeForm(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
