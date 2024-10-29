/*
 * QueryUtil.java
 *
 * Copyright 2008 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package org.dbtools.query.shared.util

/**
 * @author Jeff
 */
object QueryUtil {
    fun formatLikeClause(column: String, value: String): String {
        return "$column LIKE $value"
    }

    fun formatIgnoreCaseLikeClause(column: String, value: String): String {
        return formatLikeClause(column, value)
    }
}
