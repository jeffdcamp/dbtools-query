/*
 * JDBRecordColumnFilter.java
 *
 * Created on April 3, 2006, 12:43 PM
 *
 * Copyright 2006 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package com.jdc.db.jpa.components;

import com.jdc.components.*;
import com.jdc.db.sql.query.SQLQueryBuilder;
import com.jdc.db.sql.query.QueryCompareType;
import com.jdc.db.jpa.components.FilterComponent.FilterType;
import com.jdc.db.jpa.components.filters.JLookupComboBoxFilter;
import com.jdc.db.jpa.components.filters.JTextFieldFilter;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 *
 * @author Jeff
 */
public class JDBEntityColumnFilter {
    private Component component;
    private String columnName;
    private FilterComponent filterComponent;
    private QueryCompareType compareType;
    private int orGroupKey = SQLQueryBuilder.NO_OR_GROUP;

    private static List<FilterComponent> stdComponents = new ArrayList<FilterComponent>();
    private static List<FilterComponent> customComponents = new ArrayList<FilterComponent>();
    
    static {
        // JDC
        stdComponents.add(new JLookupComboBoxFilter());
        
        // SWING
        stdComponents.add(new JTextFieldFilter());
    }
    
    /** Creates a new instance of JDBRecordColumnFilter */
    public JDBEntityColumnFilter(Component component, String columnName, QueryCompareType compareType) {
        this(component, columnName, compareType, SQLQueryBuilder.NO_OR_GROUP);
    }
    
    /** Creates a new instance of JDBRecordColumnFilter */
    public JDBEntityColumnFilter(Component component, String columnName, QueryCompareType compareType, int orGroupKey) {
        if (component == null) {
            throw new IllegalArgumentException("component cannot be null");
        }
        this.component = component;

        if (columnName == null || columnName.length() == 0) {
            throw new IllegalArgumentException("columnName cannot be null or empty");
        }
        this.columnName = columnName;
        
        if (compareType == null) {
            compareType = QueryCompareType.LIKE;
        } else {
            this.compareType = compareType;
        }
        
        this.orGroupKey = orGroupKey;
        
        // determine what type the value is based on the component
        findValueType();
    }

    public static void addFilterComponent(FilterComponent newFilter) {
        if (newFilter == null) {
            throw new IllegalArgumentException("new filter cannot be null");
        }
        
        customComponents.add(newFilter);
    }
    
    private void findValueType() {
        for (FilterComponent filterComp : customComponents) {
            if (filterComp.isCompatableWith(component)) {
                filterComponent = filterComp;
                break;
            }
        }
        
        // if not found in custom... look in standard
        if (filterComponent == null) {
            for (FilterComponent filterComp : stdComponents) {
                if (filterComp.isCompatableWith(component)) {
                    filterComponent = filterComp;
                    break;
                }
            }
        }
        
        // make sure a filter was found for this component
        if (filterComponent == null) {
            throw new IllegalStateException("could not find a compatable FilterComponent for given component");
        }
    }

    public String getColumn() {
        return columnName;
    }
    
    public boolean ignoreComponentValue() {
        return filterComponent.ignoreComponentValue(component);
    }
    
    public Object getValue() {
        return filterComponent.getValue(component);
    }
    
    public FilterType getFilterType() {
        return filterComponent.getFilterType();
    }
    
    public QueryCompareType getCompareType() {
        return compareType;
    }

    public int getOrGroupKey() {
        return orGroupKey;
    }
}
