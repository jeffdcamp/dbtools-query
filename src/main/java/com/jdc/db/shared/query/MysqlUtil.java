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
public class MysqlUtil {

    private MysqlUtil() {
    }

    public static String formatLikeClause(String column, String value) {
        StringBuffer sb = new StringBuffer(value.length());
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '%') {
                sb.append('\\');
            }

            sb.append(chars[i]);
        }
        return QueryUtil.formatLikeClause(column, sb.toString());
    }

    public String formatIgnoreCaseLikeClause(String column, String value) {
        return formatLikeClause(column, value);
    }
}
