/*
 * FirebirdUtil.java
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
public class FirebirdUtil {

    private FirebirdUtil() {
    }
    
    public static String formatIgnoreCaseLikeClause(String column, String value) {
        boolean before = false;
        if( value.indexOf('*') == 0 ) {
            before = true;
            value = value.substring(1);
        }
        boolean after = false;
        if( value.lastIndexOf('*') == (value.length() - 1) ) {
            after = true;
            value = value.substring(0, value.length() - 1);
        }
        StringBuffer select = new StringBuffer(column);
        if ( before == after )   // default or two explicit stars
        {
            select.append(" containing '");
            select.append(QueryUtil.formatString(value, false));
            select.append("'");
        } else if ( before ) {
            select.append(" containing '");
            select.append(QueryUtil.formatString(value, false));
            select.append("'");
        } else  // if endStar
        {
            select.append(" containing '");
            select.append(QueryUtil.formatString(value, false));
            select.append("'");
        }
        return select.toString();
    }
}
