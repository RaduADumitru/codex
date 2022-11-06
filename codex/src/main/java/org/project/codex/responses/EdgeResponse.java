package org.project.codex.responses;

public class EdgeResponse {
    private String from;
    private String to;

    public EdgeResponse(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public EdgeResponse() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "EdgeResponse{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }
}
