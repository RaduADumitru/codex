package org.project.codex.enums;

public enum SqlDataType {
    VARCHAR(ArangoDataType.STRING),
    CHAR(ArangoDataType.STRING),
    MEDIUMTEXT(ArangoDataType.STRING),
    LONGTEXT(ArangoDataType.STRING),
    DATE(ArangoDataType.STRING),
    TIMESTAMP(ArangoDataType.STRING),
    INT(ArangoDataType.NUMBER),
    BIGINT(ArangoDataType.NUMBER),
    SMALLINT(ArangoDataType.NUMBER),
    FLOAT(ArangoDataType.NUMBER),
    TINYINT(ArangoDataType.BOOLEAN);

    private final ArangoDataType arangoDataType;

    SqlDataType(ArangoDataType arangoDataType) {
        this.arangoDataType = arangoDataType;
    }

    public ArangoDataType getArangoDataType() {
        return arangoDataType;
    }
}
