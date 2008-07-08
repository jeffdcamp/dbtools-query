
package com.jdc.db.jpa;

import com.jdc.db.jpa.components.JDBEntityColumnFilter;
import com.jdc.db.jpa.components.JPAEntityTable;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jeff
 */
public class TableGroup {

    private Component parent;
    private JPAEntityTable table;
    private List<JDBEntityColumnFilter> componentFilters;
    private EditableEntityListFilter filter;
    private boolean columnsAlreadyAdded = false;

    public TableGroup(JPAEntityTable table, Component parent, List<JDBEntityColumnFilter> componentFilters, EditableEntityListFilter filter) {
        this.setTable(table);
        this.setComponentFilters(componentFilters);
        this.setFilter(filter);

        if (parent == null) {
            throw new IllegalStateException("parent cannot be null");
        }

        if (parent instanceof Dialog || parent instanceof Frame) {
            this.setParent(parent);
        } else {
            throw new IllegalArgumentException("parent must be either a Dialog or Frame");
        }
    }

    public Component getParent() {
        return parent;
    }

    public void setParent(Component parent) {
        this.parent = parent;
    }

    public JPAEntityTable getTable() {
        return table;
    }

    public void setTable(JPAEntityTable table) {
        this.table = table;
    }

    public List<JDBEntityColumnFilter> getComponentFilters() {
        return componentFilters;
    }

    public void setComponentFilters(List<JDBEntityColumnFilter> componentFilters) {
        if (componentFilters != null) {
            this.componentFilters = componentFilters;
        } else {
            this.componentFilters = new ArrayList<JDBEntityColumnFilter>();
        }
    }

    public boolean isColumnsAlreadyAdded() {
        return columnsAlreadyAdded;
    }

    public void setColumnsAlreadyAdded(boolean columnsAlreadyAdded) {
        this.columnsAlreadyAdded = columnsAlreadyAdded;
    }

    public EditableEntityListFilter getFilter() {
        return filter;
    }

    public void setFilter(EditableEntityListFilter filter) {
        this.filter = filter;
    }
}
