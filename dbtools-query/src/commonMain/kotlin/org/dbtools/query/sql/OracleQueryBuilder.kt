/*
 * Copyright 2009 Jeff Campbell. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package org.dbtools.query.sql

import org.dbtools.query.shared.util.OracleUtil

/**
 *
 * @author jeff
 */
class OracleQueryBuilder : SQLQueryBuilder() {
    override fun formatIgnoreCaseLikeClause(field: String, value: String): String {
        return OracleUtil.formatIgnoreCaseLikeClause(field, value)
    }
}
