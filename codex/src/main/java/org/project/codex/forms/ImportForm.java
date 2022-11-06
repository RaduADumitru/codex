package org.project.codex.forms;

public class ImportForm {
    private boolean complete;
    private Integer pageCount;

    public ImportForm(boolean complete, Integer pageCount) {
        this.complete = complete;
        this.pageCount = pageCount;
    }

    public ImportForm() {
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }
}
