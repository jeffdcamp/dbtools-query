package org.dbtools.query.shared;

public enum JoinType {
    JOIN("JOIN"), LEFT_JOIN("LEFT JOIN"), RIGHT_JOIN("RIGHT_JOIN");

    private String joinText;

    JoinType(String joinText) {
        this.joinText = joinText;
    }

    public String getJoinText() {
        return joinText;
    }
}
