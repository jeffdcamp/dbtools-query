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
    
    private boolean isJoin = false;
    private String joinObjectClassName;
    private String joinObjectName;
    private String joinObjectVarName;
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
    
    /**
     * Allows a var/column from a different table to to show in the results
     * 
     * Example: SELECT dflt.name, addr.name FROM Person dflt, Address addr
     *                WHERE dflt.id = addr.person_id
     * 
     * Example new JDBEntityColumn(10, "Address", "addr", "name", "person_id", "id", "Address Name", 100);
     * 
     * @param jtable2ColID
     * @param joinObjectClassName, Name of join table Object ("Address")
     * @param joinObjectName, Name of join table Object (using "addr" will produce "Address addr" in final query)
     * @param joinObjectVarName, DATA TO BE SHOWN IN FINAL QUERY.  Name of var from join table (using "name" will produce "addr.name" in final query)
     * @param joinObjectJoinVarName, Name of var to join to in other table/object (using "persion_id" will produce "addr.person_id")
     * @param joinColumnName, Name of var in dflt table to join with other table (using "id" will produce "dflt.id" in final query)
     * @param columnName, Name of JTable column name
     * @param colWidth, Width of JTable column width
     */
    public JDBEntityColumn(int jtable2ColID, String joinObjectClassName, String joinObjectName, String joinObjectVarName, String joinObjectJoinVarName, String joinColumnName, String columnName, int colWidth) {
        this.jtable2ColID = jtable2ColID;
        this.columnName = columnName;
        this.columnWidth = colWidth;
        
        isJoin = true;
        if (joinObjectClassName.length() == 0 || joinObjectName.length() == 0 || joinObjectVarName.length() == 0 || joinObjectJoinVarName.length() == 0 || joinColumnName.length() == 0) {
            throw new IllegalArgumentException("all parameters must not be empty");
        }
        this.classVarName = joinObjectVarName;
        this.joinObjectVarName = joinObjectVarName;
        this.joinObjectClassName = joinObjectClassName;
        this.joinObjectName = joinObjectName;
        this.joinFromField = joinObjectName +"."+ joinObjectJoinVarName;
        this.joinToField = JPAQueryBuilder.DEFAULT_OBJ_VAR +"."+ joinColumnName;
    }
    
    
//    private void addJoin(String joinToTable, String joinFromField, String joinToField) {
//        if (joinToTable.length() == 0 || joinFromField.length() == 0 || joinToField.length() == 0) {
//            throw new IllegalArgumentException("all parameters must not be empty");
//        }
//        
//        this.joinToObject = joinToTable;
//        this.joinFromField = joinFromField;
//        this.joinToField = joinToField;
//        hasJoin = true;
//    }
    
    public boolean isJoin() {
        return isJoin;
    }
    
    public void addJoinToQuery(JPAQueryBuilder qb) {
        if (qb == null) {
            throw new NullPointerException("qb cannot be null");
        }
        
        qb.addObject(joinObjectClassName, joinObjectName);
        qb.addField(joinObjectName, joinObjectVarName);
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

    public String getJoinFromField() {
        return joinFromField;
    }

    public String getJoinObjectClassName() {
        return joinObjectClassName;
    }

    public String getJoinObjectName() {
        return joinObjectName;
    }

    public String getJoinObjectVarName() {
        return joinObjectVarName;
    }

    public String getJoinToField() {
        return joinToField;
    }
    
    
}
