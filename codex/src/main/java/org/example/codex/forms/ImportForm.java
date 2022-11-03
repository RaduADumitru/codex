package org.example.codex.forms;

public class ImportForm {
    private boolean complete;

    public ImportForm(boolean complete) {
        this.complete = complete;
    }

    public ImportForm() {
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
