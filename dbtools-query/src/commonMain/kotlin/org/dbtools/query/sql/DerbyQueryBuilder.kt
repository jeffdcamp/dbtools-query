/*
 * DerbyQueryBuilder.java
 *
 * Created on October 27, 2007
 *
 * Copyright 2007 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package org.dbtools.query.sql

import org.dbtools.query.shared.util.DerbyUtil

/**
 *
 * @author Jeff
 */
class DerbyQueryBuilder : SQLQueryBuilder() {
    override fun formatIgnoreCaseLikeClause(field: String, value: String): String {
        return DerbyUtil.formatIgnoreCaseLikeClause(field, value)
    }
}
