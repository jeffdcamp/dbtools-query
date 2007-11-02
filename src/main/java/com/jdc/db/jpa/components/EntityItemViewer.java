/*
 * RecordItemViewer.java
 *
 * Created on June 23, 2006
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
public interface EntityItemViewer<T extends Object> {
    /**
     * Show Frame/Dialog/Window... that will allow the user to ONLY VIEW the item.
     * NOTE:  Calling view item will NOT update/refresh lists.... call editItem(...) if you intend to modify the item.
     */
    public void viewItem(Component parent, T item);
}
