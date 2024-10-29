package org.dbtools.query.shared

enum class JoinType(val joinText: String) {
    JOIN("JOIN"), LEFT_JOIN("LEFT JOIN"), RIGHT_JOIN("RIGHT_JOIN")
}
