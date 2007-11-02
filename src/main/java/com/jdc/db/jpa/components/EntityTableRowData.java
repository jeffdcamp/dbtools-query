/*
 * DBTableRowData.java
 *
 * Created on September 8, 2005
 *
 * Copyright 2006 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */

package com.jdc.db.jpa.components;

import com.jdc.components.TableRowData;
import java.util.*;

/**
 *
 * @author Jeff
 */
public class EntityTableRowData implements TableRowData {
    
    private int idColumn = -1;
    
    private int id;
    private Map<Integer, Object> rowData = new HashMap<Integer, Object>();
    
    /** Creates a new instance of DBTableRowData */
    public EntityTableRowData(int idColumn) {
        this.idColumn = idColumn;
    }

    public Object getTableData(int columnID) {
        return rowData.get(columnID);
    }
    
    public void setTableDataID(int id) {
        this.id = id;
    }

    public int getTableDataID() {
        return id;
    }

    public void setTableDataValueAt(int columnID, Object data) {
        rowData.put(columnID, data);
    }
    
}
