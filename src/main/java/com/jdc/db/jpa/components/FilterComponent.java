/*
 * FilterComponent.java
 *
 * Created on April 7, 2006
 *
 *
 * Copyright 2006 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */

package com.jdc.db.jpa.components;

import java.awt.Component;

/**
 *
 * @author Jeff
 */
public interface FilterComponent {
    public static enum FilterType {STRING, INT, DATE};

    public FilterType getDefaultFilterType();
    
    /**
     *
     */
    public boolean isCompatableWith(Component comp, FilterType forType);
    
    /**
     * If this component is NOT set to something that the query should filter on
     * then return TRUE
     */
    public boolean ignoreComponentValue(Component comp, FilterType forType);
    
    /**
     * Return the value of this component that WILL be used in the filter.  This
     * value corresponds to the FilterType... Example:
     * return a String if FilterType.STRING
     * return a Integer if FilterType.INT
     * return a Date if FilterType.DATE
     */
    public Object getValue(Component comp, FilterType forType);
}
