/*
 * PostgresqlQueryBuilder.java
 *
 * Created on October 27, 2007
 *
 * Copyright 2007 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */

package com.jdc.db.sql.query;

import com.jdc.db.shared.query.PostgresqlUtil;

/**
 *
 * @author Jeff
 */
public class PostgresqlQueryBuilder extends SQLQueryBuilder {

    @Override
    public String formatIgnoreCaseLikeClause(String column, String value) {
        return PostgresqlUtil.formatIgnoreCaseLikeClause(column, value);
    }
    
}
