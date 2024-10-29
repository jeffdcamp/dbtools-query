/*
 * PostgresqlQueryBuilder.java
 *
 * Created on October 27, 2007
 *
 * Copyright 2007 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package org.dbtools.query.sql

import org.dbtools.query.shared.util.QueryUtil

/**
 *
 * @author Jeff
 */
class MysqlQueryBuilder : SQLQueryBuilder() {
    override fun formatLikeClause(field: String, value: String): String {
        return QueryUtil.formatLikeClause(field, value)
    }

    override fun formatIgnoreCaseLikeClause(field: String, value: String): String {
        return formatLikeClause(field, value)
    }
}
