package org.project.codex.util;

import java.util.List;

//to be used in graph creation API call
public class EdgeDefinition {
    private String collection;
    //"from" document collections in graph
    private List<String> from;
    //"to" document collections in graph
    private List<String> to;

    public EdgeDefinition(String collection, List<String> from, List<String> to) {
        this.collection = collection;
        this.from = from;
        this.to = to;
    }

    public EdgeDefinition() {
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public List<String> getFrom() {
        return from;
    }

    public void setFrom(List<String> from) {
        this.from = from;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }
}
