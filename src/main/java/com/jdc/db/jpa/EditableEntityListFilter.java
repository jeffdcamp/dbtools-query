/*
 * EditableRecordListFilter.java
 *
 * Created on August 29, 2006
 *
 * Copyright 2006 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */

package com.jdc.db.jpa;

import com.jdc.db.sql.query.QueryBuilder;

/**
 *
 * @author Jeff
 */
public interface EditableEntityListFilter {

    /**
     * @return int id of this filter... to allow other components that have the
     *         SAME EXACT filter to not have to re-query the database
     */
    public int getFilterID();
    
    /**
     * Allow the addition of additional filter
     * @param qb Existing query that contains at minumum the base querty
     */
    public void appendFilter(QueryBuilder qb);
}
