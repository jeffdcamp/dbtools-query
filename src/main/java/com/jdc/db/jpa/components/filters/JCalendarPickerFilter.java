/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jdc.db.jpa.components.filters;

import com.jdc.components.calendar.JCalendarPicker;
import com.jdc.db.jpa.components.FilterComponent;
import java.awt.Component;

/**
 *
 * @author Jeff
 */
public class JCalendarPickerFilter implements FilterComponent {

    public FilterType getDefaultFilterType() {
        return FilterType.DATE;
    }
    
    public boolean isCompatableWith(Component comp, FilterType forType) {
        if (forType == null) {
            forType = getDefaultFilterType();
        }
        
         return (forType == FilterType.DATE && comp instanceof JCalendarPicker);
    }

    public boolean ignoreComponentValue(Component comp, FilterType forType) {
        return getValue(comp, forType) == null;
    }

    public Object getValue(Component comp, FilterType forType) {
        Object value;
        switch (forType) {
            case DATE:
                value = ((JCalendarPicker)comp).getDate();
                break;
            default:
                value = null;
        }
        return value;
    }

}
