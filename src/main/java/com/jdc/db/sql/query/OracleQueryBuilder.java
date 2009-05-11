/*
 * Copyright 2009 Jeff Campbell. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package com.jdc.db.sql.query;

import com.jdc.db.shared.query.OracleUtil;

/**
 *
 * @author jeff
 */
public class OracleQueryBuilder extends SQLQueryBuilder {

    @Override
    public String formatIgnoreCaseLikeClause(String column, String value) {
        return OracleUtil.formatIgnoreCaseLikeClause(column, value);
    }
}
