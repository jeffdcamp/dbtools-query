package com.jdc.db.shared.query;

public enum QueryJoinType {
    JOIN("JOIN"), LEFT_JOIN("LEFT JOIN"), RIGHT_JOIN("RIGHT_JOIN");

    private String joinText;

    QueryJoinType(String joinText) {
        this.joinText = joinText;
    }

    public String getJoinText() {
        return joinText;
    }
}
