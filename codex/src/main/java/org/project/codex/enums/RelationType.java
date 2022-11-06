package org.project.codex.enums;

public enum RelationType {
    SYNONYM(1),
    ANTONYM(2),
    DIMINUTIVE(3),
    AUGMENTATIVE(4);
    private Integer relationTypeCode;

    RelationType(Integer relationTypeCode) {
        this.relationTypeCode = relationTypeCode;
    }

    public Integer getRelationTypeCode() {
        return relationTypeCode;
    }
}
