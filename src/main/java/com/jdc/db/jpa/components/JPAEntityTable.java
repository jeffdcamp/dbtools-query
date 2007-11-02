/*
 * JEditableDBTable.java
 *
 * Created on March 4, 2006, 12:22 AM
 *
 * Copyright 2006 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package com.jdc.db.jpa.components;

import com.jdc.components.JTable2;
import com.jdc.db.jpa.EditableEntityList;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Cherie
 */
public class JPAEntityTable<T extends Object> extends JTable2 {

    private Map<Integer, Integer> sqlColToUiColMap = new HashMap<Integer, Integer>(); 
    private Map<Integer, JDBEntityColumn> entityColumnsBySQLCol = new HashMap<Integer, JDBEntityColumn>(); 
    private EditableEntityList<T> editableEntityList;
    
    /** Creates a new instance of JEditableDBTable */
    public JPAEntityTable() {
        super.setSelectionMode(1);
    }
  
    public EditableEntityList<T> getEditableEntityList() {
        if (editableEntityList == null) {
            throw new IllegalStateException("cannot get EditableEntityList because it is null.  Be sure to call myJDBTable.setEditableRecordList(...) on the table after it is initialized");
        }
        return editableEntityList;
    }

    public void setEditableEntityList(EditableEntityList<T> editableEntityList) {
        this.editableEntityList = editableEntityList;
    }
    
    /**
     * Creates a new item. same as newItem()
     *
     * @return Newly created item or null if none
     */
    public T addItem() {
        return getEditableEntityList().addItem(this);
    }
    
    /**
     * Creates a new item. same as addItem()
     *
     * @return Newly created item or null if none
     */
    public T newItem() {
        return getEditableEntityList().addItem(this);
    }

    public T editSelectedItem() {
        return getEditableEntityList().editSelectedItem(this);
    }

    public T viewSelectedItem() {
        return getEditableEntityList().viewSelectedItem(this);
    }
    
    public boolean deleteSelectedItem() throws SQLException {
        return getEditableEntityList().deleteSelectedItem(this);
    }
    
    public void executeSearch() {
        if (editableEntityList == null) {
            throw new IllegalStateException("EditableRecordList has not been set for this table.  call setEditableRecordList(...) before executing a search");
        }
        
        getEditableEntityList().executeSearch(this);
    }
    
    public void setSqlColToUiColMap(int sqlCol, int jTable2ColID) {
        sqlColToUiColMap.put(sqlCol, jTable2ColID);
    }
    
    public int getUiColID(int sqlCol) {
        Integer uiCol = sqlColToUiColMap.get(sqlCol);
        
        if (uiCol == null) {
            throw new IllegalStateException("Could NOT find UIColumn for sqlCol ["+ sqlCol +"]");
        }
        
        return uiCol;
    }
    
    public void setJDBEntityColumnBySQLColID(int jTable2ColID, JDBEntityColumn classType) {
        entityColumnsBySQLCol.put(jTable2ColID, classType);
    }
    
    public JDBEntityColumn getJDBEntityColumnBySQLColID(int jTable2ColID) {
        return entityColumnsBySQLCol.get(jTable2ColID);
    }
}
