package org.project.codex.enums;

public enum MeaningType {
    PROPER_MEANING(0),
    ETYMOLOGY(1),
    USAGE_EXAMPLE(2),
    COMMENT(3),
    DIFF(4),
    COMPOUND_MEANING(5);
    private Integer MeaningCode;

    MeaningType(Integer meaningCode) {
        MeaningCode = meaningCode;
    }

    public Integer getMeaningCode() {
        return MeaningCode;
    }
}
