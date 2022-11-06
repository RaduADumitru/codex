package org.project.codex.forms;

public class OptimizeImportForm {
    private Integer pagecount;

    public OptimizeImportForm(Integer pagecount) {
        this.pagecount = pagecount;
    }

    public OptimizeImportForm() {
    }

    public Integer getPagecount() {
        return pagecount;
    }

    public void setPagecount(Integer pagecount) {
        this.pagecount = pagecount;
    }
}
