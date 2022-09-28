package org.example.codex.util;

import net.sf.jsqlparser.parser.StatementListener;
import net.sf.jsqlparser.statement.Statement;

public class ImportStatementListener implements StatementListener {
    @Override
    public void accept(Statement statement) {
//        if(statement instanceof CreateTable) {
//            System.out.printf("1");
//        }
//        else if(statement instanceof Insert) {
//            System.out.printf("2");
//        }
    }
}
