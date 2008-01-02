/*
 * JDBRecordColumn.java
 *
 * Created on March 5, 2006, 6:07 PM
 *
 * Copyright 2006 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package com.jdc.db.jpa.components;

import com.jdc.db.jpa.query.JPAQueryBuilder;

/**
 *
 * @author Jeff
 */
public class JDBEntityColumn {

    private int jtable2ColID = -1;
    private String columnName;
    private String classVarName;
    
    private boolean hasJoin = false;
    private String joinToObject;
    private String joinFromField;
    private String joinToField;
    
    private int columnWidth = 150;
    private boolean visible = true;
    
    private boolean quickSearchItem = true;
    private Class classType = null;
    
    /** Creates a new instance of JDBRecordColumn */
    public JDBEntityColumn(int jtable2ColID, String classVarName) {
        this.jtable2ColID = jtable2ColID;
        this.columnName = classVarName;
        this.classVarName = classVarName;
    }

    public JDBEntityColumn(int jtable2ColID, String classVarName, int colWidth) {
        this.jtable2ColID = jtable2ColID;
        this.columnName = classVarName;
        this.classVarName = classVarName;
        this.columnWidth = colWidth;
    }
    
    public JDBEntityColumn(int jtable2ColID, String classVarName, String columnName) {
        this.jtable2ColID = jtable2ColID;
        this.columnName = columnName;
        this.classVarName = classVarName;
    }
    
    public JDBEntityColumn(int jtable2ColID, String classVarName, String columnName, int colWidth) {
        this.jtable2ColID = jtable2ColID;
        this.columnName = columnName;
        this.classVarName = classVarName;
        this.columnWidth = colWidth;
    }
    
    public void addJoin(String joinToTable, String joinFromField, String joinToField) {
        if (joinToTable.length() == 0 || joinFromField.length() == 0 || joinToField.length() == 0) {
            throw new IllegalArgumentException("all parameters must not be empty");
        }
        
        this.joinToObject = joinToTable;
        this.joinFromField = joinFromField;
        this.joinToField = joinToField;
        hasJoin = true;
    }
    
    public boolean hasJoin() {
        return hasJoin;
    }
    
    public void addJoinToQuery(JPAQueryBuilder qb) {
        if (qb == null) {
            throw new NullPointerException("qb cannot be null");
        }
        
        qb.addObject(joinToObject);
        qb.addJoin(joinFromField, joinToField);
    }

    public int getJTable2ColID() {
        return jtable2ColID;
    }

    public void setJTable2ColID(int jtable2ColID) {
        this.jtable2ColID = jtable2ColID;
    }
    
    
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getVarClassName() {
        return classVarName;
    }

    public void setClassVarName(String classVarName) {
        this.classVarName = classVarName;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public void setColumnWidth(int columnWidth) {
        this.columnWidth = columnWidth;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isQuickSearchItem() {
        return quickSearchItem;
    }

    public void setQuickSearchItem(boolean quickSearchItem) {
        this.quickSearchItem = quickSearchItem;
    }

    public Class getClassType() {
        return classType;
    }

    /**
     * Forces the results from a createNativeQuery on JPA to cast this column
     * to the specified ClassType
     */
    public void setClassType(Class classType) {
        this.classType = classType;
    }
}
