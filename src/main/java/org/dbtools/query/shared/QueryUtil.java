/*
 * QueryUtil.java
 *
 * Copyright 2008 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package org.dbtools.query.shared;

/**
 * @author Jeff
 */
public final class QueryUtil {


    private QueryUtil() {}

    public static String formatLikeClause(String column, String value) {
        return column + " LIKE " + value;
    }

    public static String formatIgnoreCaseLikeClause(String column, String value) {
        return formatLikeClause(column, value);
    }
}
