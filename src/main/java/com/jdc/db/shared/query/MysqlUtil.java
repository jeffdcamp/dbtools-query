/*
 * MysqlUtil.java
 *
 * Created on October 27, 2007
 *
 * Copyright 2007 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package com.jdc.db.shared.query;

/**
 *
 * @author Jeff
 */
public final class MysqlUtil {

    private MysqlUtil() {
    }

    public static String formatLikeClause(String column, String value) {
        StringBuilder sb = new StringBuilder(value.length());
        char[] chars = value.toCharArray();
        for (char aChar : chars) {
            if (aChar == '%') {
                sb.append('\\');
            }

            sb.append(aChar);
        }
        return QueryUtil.formatLikeClause(column, sb.toString());
    }

    public String formatIgnoreCaseLikeClause(String column, String value) {
        return formatLikeClause(column, value);
    }
}
