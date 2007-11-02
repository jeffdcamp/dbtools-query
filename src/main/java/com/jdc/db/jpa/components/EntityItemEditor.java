/*
 * RecordItemEditor.java
 *
 * Created on June 22, 2006
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
public interface EntityItemEditor<T extends Object> {
    /**
     * Show Frame/Dialog/Window... that will allow the user to EDIT the item.
     * 
     * NOTE: If your edit screen is NOT a modal (Frame, JPanel, etc) OR you did
     * not finish modifing the item before editItem leaves scope.... return false
     * AND call myList.refreshList().
     *
     * @return true IF, and ONLY IF the item has finished being edited AND the item has been saved! (including entityManager.commit())
     */
    public boolean editItem(Component parent, T item);
}
