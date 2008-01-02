/*
 * JLookupComboBoxFilter.java
 *
 * Created on April 7, 2006
 *
 *
 * Copyright 2006 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package com.jdc.db.jpa.components.filters;

import com.jdc.components.JLookupComboBox;
import com.jdc.db.jpa.components.FilterComponent;
import java.awt.Component;
import javax.swing.*;

/**
 *
 * @author Jeff
 */
public class JLookupComboBoxFilter implements FilterComponent {

    public FilterType getDefaultFilterType() {
        return FilterType.INT;
    }
    
    public boolean isCompatableWith(Component comp, FilterType forType) {
        if (forType == null) {
            forType = getDefaultFilterType();
        }
        
        return (comp instanceof JLookupComboBox && (forType == FilterType.INT || forType == FilterType.STRING));
    }

    public boolean ignoreComponentValue(Component comp, FilterType forType) {
        boolean ignore = false;
        switch (forType) {
            case INT:
                ignore = ((JLookupComboBox) comp).getSelectedID() > -1;
                break;
            case STRING:
                ignore = ((JLookupComboBox) comp).getText().trim().length() == 0;
                break;
            default:
                ignore = true;
        }
        return ignore;
    }

    public Object getValue(Component comp, FilterType forType) {
        Object value;
        switch (forType) {
            case INT:
                value = ((JLookupComboBox) comp).getSelectedID();
                break;
            case STRING:
                value = ((JLookupComboBox) comp).getText().trim().length();
                break;
            default:
                value = null;
        }
        return value;
    }
}
