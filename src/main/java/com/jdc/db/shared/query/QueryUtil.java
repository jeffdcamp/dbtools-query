/*
 * QueryUtil.java
 *
 * Copyright 2008 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package com.jdc.db.shared.query;

/**
 *
 * @author Jeff
 */
public final class QueryUtil {

    private QueryUtil() {
    }

    public static String formatString(String str) {
        return formatString(str, true);
    }

    public static String formatString(String str, boolean wrap) {
        int length = str.length();
        char[] input = str.toCharArray();
        StringBuilder temp = new StringBuilder(length + 2);

        if (wrap) {
            temp.append('\'');
        }  // opening quote

        for (int i = 0; i < length; i++) {
            if (input[i] == '\'') {
                temp.append('\'');
            }  // make the quote a literal

            temp.append(input[i]);
        }

        if (wrap) {
            temp.append('\'');
        }  // closing quote

        return temp.toString();
    }

    public static String formatLikeClause(String column, String value) {
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
        StringBuilder select = new StringBuilder(column);
        if (before == after) { // default or two explicit stars
            select.append(" LIKE '%");
            select.append(formatString(value, false));
            select.append("%'");
        } else if (before) {
            select.append(" LIKE '%");
            select.append(formatString(value, false));
            select.append("'");
        } else { // if endStar
            select.append(" LIKE '");
            select.append(formatString(value, false));
            select.append("%'");
        }
        return select.toString();
    }

    public static String formatIgnoreCaseLikeClause(String column, String value) {
        return formatLikeClause(column, value);
    }
}
