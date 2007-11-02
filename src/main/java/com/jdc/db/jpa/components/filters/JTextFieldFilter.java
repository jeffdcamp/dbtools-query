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
    
    public FilterComponent.FilterType getFilterType() {
        return FilterType.STRING;
    }

    public boolean isCompatableWith(Component comp) {
        return comp instanceof JTextField;
    }
    
    public boolean ignoreComponentValue(Component comp) {
        return ((JTextField)comp).getText().trim().length() == 0;
    }

    public Object getValue(Component comp) {
        return ((JTextField)comp).getText().trim();
    }
}
