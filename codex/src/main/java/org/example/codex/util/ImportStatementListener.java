package org.example.codex.util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.ValueListExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.parser.StatementListener;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.math3.analysis.function.Exp;

import java.util.ArrayList;
import java.util.List;

public class ImportStatementListener implements StatementListener {
    @Override
    public void accept(Statement statement) {
        //Send requests to database server
        if(statement instanceof CreateTable) {
            System.out.println("Create: " + ((CreateTable) statement).getTable());
        }
        else if(statement instanceof Insert insert) {
            ArrayList<String> tables = new ArrayList<>(List.of(new String[]{"AdsLink"}));
            if(tables.contains(insert.getTable().getName())) {
                ItemsList itemsList = insert.getItemsList();
                ExpressionList expressionList = (ExpressionList) itemsList;
                for(Expression expression : expressionList.getExpressions()) {
                    RowConstructor rowConstructor = (RowConstructor) expression;
                    List<Expression> values = rowConstructor.getExprList().getExpressions();
                    for(Expression value : values) {
                        if(value instanceof StringValue stringValue) {
                            System.out.println(stringValue);
                        }
                    }
                    System.out.println(rowConstructor.getExprList().getExpressions().get(0));
                }
            }

//            ValueListExpression valueListExpression = (ValueListExpression) first;
//            System.out.println(valueListExpression.getExpressionList().getExpressions().size());
//            ValueListExpression valueListExpression = (ValueListExpression) first;
// Not working
//            SimpleNode node = first.getASTNode();
//            System.out.println("Value: " + node.jjtGetValue());
//            System.out.println("Parent: " + node.jjtGetParent());
//            System.out.println("Child: " + node.jjtGetChild(0));


//            valueListExpression.
//            System.out.println(first.);

//            System.out.println("Insert into: " + ((Insert) statement).getSelect());
//            SelectBody selectBody = insert.getSelect().getSelectBody();
//            SetOperationList setOperationList = (SetOperationList) selectBody;
//            System.out.println("INSERT: " + setOperationList.getSelects().get(0).);


//            SelectBody selectBody = ((Insert)statement).getSelect().getSelectBody();
//            SetOperationList setOperationList = (SetOperationList)selectBody;
//            List<SelectBody> selectBodies = setOperationList.getSelects();
//            System.out.println("Operations: " + selectBodies.get(0).); //empty

//            System.out.println(plainSelect.getSelectItems().get(0));
//            SelectVisitor
//            selectBody.
//            List<SelectItem> selectItems = plainSelect.getSelectItems();
//            System.out.println(selectItems.get(0));
//            System.out.println((PlainSelect)((Insert) statement).getSelect().getSelectBody());
        }
    }
}
