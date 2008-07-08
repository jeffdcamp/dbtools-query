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

import com.jdc.db.jpa.components.filters.JCalendarPickerFilter;
import com.jdc.db.sql.query.SQLQueryBuilder;
import com.jdc.db.shared.query.QueryCompareType;
import com.jdc.db.jpa.components.FilterComponent.FilterType;
import com.jdc.db.jpa.components.filters.JLookupComboBoxFilter;
import com.jdc.db.jpa.components.filters.JTextFieldFilter;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jeff
 */
public class JDBEntityColumnFilter {
    private FilterType filterType;
    private Component component;
    private String objectVarName = null;
    private String varName;
    private FilterComponent filterComponent;
    private QueryCompareType compareType;
    private int orGroupKey = SQLQueryBuilder.NO_OR_GROUP;

    private static List<FilterComponent> stdComponents = new ArrayList<FilterComponent>();
    private static List<FilterComponent> customComponents = new ArrayList<FilterComponent>();
    
    static {
        // JDC
        stdComponents.add(new JLookupComboBoxFilter());
        stdComponents.add(new JCalendarPickerFilter());
        
        // SWING
        stdComponents.add(new JTextFieldFilter());
    }
    
    /** Creates a new instance of JDBRecordColumnFilter */
    public JDBEntityColumnFilter(Component component, String varName, QueryCompareType compareType) {
        this(null, component, varName, compareType, SQLQueryBuilder.NO_OR_GROUP);
    }
    
    public JDBEntityColumnFilter(Component component, String objectVarName, String varName, QueryCompareType compareType) {
        this(null, component, objectVarName, varName, compareType, SQLQueryBuilder.NO_OR_GROUP);
    }
    
    
    
    /** Creates a new instance of JDBRecordColumnFilter */
    public JDBEntityColumnFilter(FilterType forType, Component component, String varName, QueryCompareType compareType) {
        this(forType, component, varName, compareType, SQLQueryBuilder.NO_OR_GROUP);
    }
    
    public JDBEntityColumnFilter(FilterType forType, Component component, String objectVarName, String varName, QueryCompareType compareType) {
        this(forType, component, objectVarName, varName, compareType, SQLQueryBuilder.NO_OR_GROUP);
    }
    
    /** Creates a new instance of JDBRecordColumnFilter */
    public JDBEntityColumnFilter(FilterType forType, Component component, String varName, QueryCompareType compareType, int orGroupKey) {
        this(forType, component, null, varName, compareType, orGroupKey);
    }
    
    /** Creates a new instance of JDBRecordColumnFilter */
    public JDBEntityColumnFilter(FilterType forType, Component component, String objectVarName, String varName, QueryCompareType compareType, int orGroupKey) {
        this.filterType = forType;
        if (component == null) {
            throw new IllegalArgumentException("component cannot be null");
        }
        this.component = component;

        if (varName == null || varName.length() == 0) {
            throw new IllegalArgumentException("columnName cannot be null or empty");
        }
        this.varName = varName;
        
        if (objectVarName != null && objectVarName.length() == 0) {
            this.objectVarName = null;
        } else {
            this.objectVarName = objectVarName;
        }
        
        if (compareType == null) {
            compareType = QueryCompareType.LIKE;
        } else {
            this.compareType = compareType;
        }
        
        this.orGroupKey = orGroupKey;
        
        // determine what type the value is based on the component
        findValueType(filterType);
    }

    public static void addFilterComponent(FilterComponent newFilter) {
        if (newFilter == null) {
            throw new IllegalArgumentException("new filter cannot be null");
        }
        
        customComponents.add(newFilter);
    }
    
    private void findValueType(FilterType forType) {
        for (FilterComponent filterComp : customComponents) {
            if (filterComp.isCompatableWith(component, forType)) {
                filterComponent = filterComp;
                break;
            }
        }
        
        // if not found in custom... look in standard
        if (filterComponent == null) {
            for (FilterComponent filterComp : stdComponents) {
                if (filterComp.isCompatableWith(component, forType)) {
                    filterComponent = filterComp;
                    break;
                }
            }
        }
        
        // make sure a filter was found for this component
        if (filterComponent == null) {
            throw new IllegalStateException("could not find a compatable FilterComponent for given component");
        }
        
        if (forType == null) {
            filterType = filterComponent.getDefaultFilterType();
        }
    }

    public void setObjectVarName(String objectVarName) {
        this.objectVarName = objectVarName;
    }

    public String getObjectVarName() {
        return objectVarName;
    }
    
    public String getVarName() {
        return varName;
    }
    
    public boolean ignoreComponentValue() {
        FilterType typeToUse;
        if (filterType == null) {
            typeToUse = filterComponent.getDefaultFilterType();
        } else {
            typeToUse = filterType;
        }
        return filterComponent.ignoreComponentValue(component, typeToUse);
    }
    
    public Object getValue() {
        FilterType typeToUse;
        if (filterType == null) {
            typeToUse = filterComponent.getDefaultFilterType();
        } else {
            typeToUse = filterType;
        }
        return filterComponent.getValue(component, typeToUse);
    }
    
    public FilterType getFilterType() {
        return filterType;
    }
    
    public QueryCompareType getCompareType() {
        return compareType;
    }

    public int getOrGroupKey() {
        return orGroupKey;
    }
}
