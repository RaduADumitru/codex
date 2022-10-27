package org.example.codex.enums;

public enum SqlDataTypes {
    VARCHAR(ArangoDataTypes.STRING),
    CHAR(ArangoDataTypes.STRING),
    MEDIUMTEXT(ArangoDataTypes.STRING),
    LONGTEXT(ArangoDataTypes.STRING),
    DATE(ArangoDataTypes.STRING),
    TIMESTAMP(ArangoDataTypes.STRING),
    INT(ArangoDataTypes.NUMBER),
    BIGINT(ArangoDataTypes.NUMBER),
    SMALLINT(ArangoDataTypes.NUMBER),
    FLOAT(ArangoDataTypes.NUMBER),
    TINYINT(ArangoDataTypes.BOOLEAN);

    private final ArangoDataTypes arangoDataType;

    SqlDataTypes(ArangoDataTypes arangoDataType) {
        this.arangoDataType = arangoDataType;
    }

    public ArangoDataTypes getArangoDataType() {
        return arangoDataType;
    }
}
