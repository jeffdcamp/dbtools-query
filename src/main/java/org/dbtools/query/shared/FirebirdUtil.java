/*
 * FirebirdUtil.java
 *
 * Created on October 27, 2007
 *
 * Copyright 2007 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package org.dbtools.query.shared;

/**
 *
 * @author Jeff
 */
public final class FirebirdUtil {

    private FirebirdUtil() {
    }

    public static String formatIgnoreCaseLikeClause(String column, String value) {
        return column + " containing " + value;
    }
}
