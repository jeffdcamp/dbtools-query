/*
 * FirebirdQueryBuilder.java
 *
 * Copyright 2008 Jeff Campbell. All rights reserved. Unauthorized reproduction 
 * is a violation of applicable law. This material contains certain 
 * confidential or proprietary information and trade secrets of Jeff Campbell.
 */
package com.jdc.db.jpa.query;

import com.jdc.db.shared.query.FirebirdUtil;

/**
 *
 * @author Jeff
 */
public class FirebirdQueryBuilder extends JPAQueryBuilder {

    @Override
    public String formatIgnoreCaseLikeClause(String column, String value) {
        return FirebirdUtil.formatIgnoreCaseLikeClause(column, value);
    }
}
