package org.example.codex.util;

import net.sf.jsqlparser.expression.Expression;
import org.example.codex.enums.ArangoDataTypes;

import java.util.Objects;

public class ExpressionDataTypeTuple {
    private Expression expression;
    private ArangoDataTypes dataType;

    public ExpressionDataTypeTuple(Expression expression, ArangoDataTypes dataType) {
        this.expression = expression;
        this.dataType = dataType;
    }

    public ExpressionDataTypeTuple() {
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public ArangoDataTypes getDataType() {
        return dataType;
    }

    public void setDataType(ArangoDataTypes dataType) {
        this.dataType = dataType;
    }

    @Override
    public String toString() {
        return "ExpressionDataTypeTuple{" +
                "expression=" + expression +
                ", dataType=" + dataType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionDataTypeTuple that = (ExpressionDataTypeTuple) o;
        return Objects.equals(expression, that.expression) && dataType == that.dataType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, dataType);
    }
}
