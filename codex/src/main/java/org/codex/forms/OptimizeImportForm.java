package org.codex.forms;

public class OptimizeImportForm {
    private Integer pageCount;

    public OptimizeImportForm(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public OptimizeImportForm() {
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }
}
