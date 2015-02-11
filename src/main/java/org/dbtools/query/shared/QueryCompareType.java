/*
 * QueryCompareType.java
 *
 * Created on October 27, 2007
 *
 * Copyright 2007 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package org.dbtools.query.shared;

public enum QueryCompareType {
    NONE, EQUAL, NOT_EQUAL, LESSTHAN, GREATERTHAN, LESSTHAN_EQUAL, GREATERTHAN_EQUAL, LIKE, LIKE_IGNORECASE, IN, IS_NULL, NOT_NULL
}
