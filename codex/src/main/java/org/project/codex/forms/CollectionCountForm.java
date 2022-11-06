package org.project.codex.forms;

public class CollectionCountForm {
    private String collection;

    public CollectionCountForm(String collection) {
        this.collection = collection;
    }

    public CollectionCountForm() {
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }
}
