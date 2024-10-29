/*
 * Copyright 2009 Jeff Campbell. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package org.dbtools.query.shared.util

/**
 *
 * @author jeff
 */
object OracleUtil {
    fun formatIgnoreCaseLikeClause(column: String, value: String): String {
        return "REGEXP_LIKE($column, $value, 'i')"
    }
}
