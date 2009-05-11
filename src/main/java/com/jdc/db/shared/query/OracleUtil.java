/*
 * Copyright 2009 Jeff Campbell. All rights reserved. Unauthorized reproduction
 * is a violation of applicable law. This material contains certain
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package com.jdc.db.shared.query;

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
        boolean before = false;
        if (value.indexOf('*') == 0) {
            before = true;
            value = value.substring(1);
        }
        boolean after = false;
        if (value.lastIndexOf('*') == (value.length() - 1)) {
            after = true;
            value = value.substring(0, value.length() - 1);
        }
        StringBuffer select = new StringBuffer("lower("+column+")");
        if (before == after) // default or two explicit stars
        {
            select.append(" LIKE '%");
            select.append(QueryUtil.formatString(value, false).toLowerCase());
            select.append("%'");
        } else if (before) {
            select.append(" LIKE '%");
            select.append(QueryUtil.formatString(value, false).toLowerCase());
            select.append("'");
        } else // if endStar
        {
            select.append(" LIKE '");
            select.append(QueryUtil.formatString(value, false).toLowerCase());
            select.append("%'");
        }
        return select.toString();
    }
}
