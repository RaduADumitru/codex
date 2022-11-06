package org.project.codex.forms;

public class ImportForm {
    private boolean complete;
    private Integer pagecount;

    public ImportForm(boolean complete, Integer pagecount) {
        this.complete = complete;
        this.pagecount = pagecount;
    }

    public ImportForm() {
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public Integer getPagecount() {
        return pagecount;
    }

    public void setPagecount(Integer pagecount) {
        this.pagecount = pagecount;
    }
}
