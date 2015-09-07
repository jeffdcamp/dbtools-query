package org.dbtools.query.shared;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class QueryBuilder {
    public abstract String formatLikeClause(String field, String value);

    public abstract String formatIgnoreCaseLikeClause(String field, String value);

    public abstract Object formatValue(Object value);

    public abstract String getQueryParameter();

    public abstract String buildQuery();

    public static String[] toSelectionArgs(Object... args) {
        List<String> selectionArgs = new ArrayList<String>(args.length);
        for (Object o : args) {
            if (o instanceof List) {
                for (Object p : (List) o) {
                    selectionArgs.add(String.valueOf(p));
                }
            } else {
                selectionArgs.add(String.valueOf(o));
            }
        }
        return selectionArgs.toArray(new String[selectionArgs.size()]);
    }
}
