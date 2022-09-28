package org.example.codex.forms;

import java.util.ArrayList;

public class ImportForm {
    private ArrayList<ArrayList> content;

    public ImportForm(ArrayList<ArrayList> content) {
        this.content = content;
    }

    public ArrayList<ArrayList> getContent() {
        return content;
    }

    public void setContent(ArrayList<ArrayList> content) {
        this.content = content;
    }
    public void addRow(ArrayList row) {
        content.add(row);
    }
}
