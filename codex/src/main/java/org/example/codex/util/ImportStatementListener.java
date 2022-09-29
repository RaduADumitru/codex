package org.example.codex.util;

import net.sf.jsqlparser.parser.StatementListener;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;

import java.util.List;

public class ImportStatementListener implements StatementListener {
    @Override
    public void accept(Statement statement) {
        //Send requests to database server corresponding to each statement
        if(statement instanceof CreateTable) {
            System.out.println("Create: " + ((CreateTable) statement).getTable());
        }
        else if(statement instanceof Insert) {
//            System.out.println("Insert into: " + ((Insert) statement).getSelect());
            SelectBody selectBody = ((Insert)statement).getSelect().getSelectBody();
            PlainSelect plainSelect = (PlainSelect)selectBody;
            System.out.println(plainSelect.getSelectItems().get(0));
//            SelectVisitor
//            selectBody.
//            List<SelectItem> selectItems = plainSelect.getSelectItems();
//            System.out.println(selectItems.get(0));
//            System.out.println((PlainSelect)((Insert) statement).getSelect().getSelectBody());
        }
    }
}
