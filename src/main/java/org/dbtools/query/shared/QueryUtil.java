/*
 * QueryUtil.java
 *
 * Copyright 2008 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package org.dbtools.query.shared;

import javax.persistence.Query;
import java.util.List;

/**
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
        return column + " LIKE '" + formatString(value, false) + "'";
    }

    public static String formatIgnoreCaseLikeClause(String column, String value) {
        return formatLikeClause(column, value);
    }

    public static <T> T getSingleResult(Query query) {
        query.setMaxResults(1);
        List<?> list = query.getResultList();
        if (list == null || list.size() == 0) {
            return null;
        }
        return (T) list.get(0);
    }
}
