package org.example.codex.forms;

public class MeaningsForm {
    private String name;
    private Integer type;

    public MeaningsForm(String name, Integer type) {
        this.name = name;
        this.type = type;
    }

    public MeaningsForm() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
