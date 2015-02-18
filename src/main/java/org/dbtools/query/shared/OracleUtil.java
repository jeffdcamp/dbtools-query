/*
 * Copyright 2009 Jeff Campbell. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package org.dbtools.query.shared;

/**
 *
 * @author jeff
 */
public final class OracleUtil {

    private OracleUtil() {
    }

    public static String formatIgnoreCaseLikeClause(String column, String value) {
        return "REGEXP_LIKE(" + column + ", '" + value + "', 'i')";
    }

    public static String formatIgnoreCaseLikeClauseJPA(String column, String value) {
        return "lower(" + column + ") LIKE '" + QueryUtil.formatString(value, false).toLowerCase() + "'";
    }
}
