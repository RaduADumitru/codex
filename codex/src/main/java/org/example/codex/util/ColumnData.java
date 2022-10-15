package org.example.codex.util;

import org.example.codex.enums.ArangoDataTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ColumnData implements Comparable<ColumnData>{

    private String columnName;
    private Integer position;
    private ArangoDataTypes dataType;

    public ColumnData() {
    }

    public ColumnData(String columnName, Integer position, ArangoDataTypes dataType) {
        this.columnName = columnName;
        this.position = position;
        this.dataType = dataType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public ArangoDataTypes getDataType() {
        return dataType;
    }

    public void setDataType(ArangoDataTypes dataType) {
        this.dataType = dataType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnData that = (ColumnData) o;
        return columnName.equals(that.columnName) && position.equals(that.position) && dataType == that.dataType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnName, position, dataType);
    }

    @Override
    public int compareTo(@NotNull ColumnData o) {
        if(Objects.equals(this.position, o.position)) {
            if(Objects.equals(this.columnName, o.columnName)) {
                return this.dataType.compareTo(o.dataType);
            }
            else return this.columnName.compareTo(o.columnName);
        }
        else return this.position.compareTo(o.position);
    }
}
