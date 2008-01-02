/*
 * JTextFieldFilter.java
 *
 * Created on April 7, 2006
 *
 *
 * Copyright 2006 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */

package com.jdc.db.jpa.components.filters;

import com.jdc.db.jpa.components.FilterComponent;
import java.awt.Component;
import javax.swing.*;

/**
 *
 * @author Jeff
 */
public class JTextFieldFilter implements FilterComponent {
    
    public FilterType getDefaultFilterType() {
        return FilterType.STRING;
    }
    
    public boolean isCompatableWith(Component comp, FilterType forType) {
        if (forType == null) {
            forType = getDefaultFilterType();
        }
        
        return comp instanceof JTextField && forType == FilterType.STRING;
    }
    
    public boolean ignoreComponentValue(Component comp, FilterType forType) {
        return ((String)getValue(comp, forType)).length() == 0;
    }

    public Object getValue(Component comp, FilterType forType) {
        return ((JTextField)comp).getText().trim();
    }
}
