package plus.datacenter.core.utils;

import plus.datacenter.core.entities.Rangeable;
import plus.datacenter.core.entities.queries.Query;
import plus.datacenter.core.entities.queries.QueryOperation;
import plus.datacenter.core.entities.queries.queries.BetweenQuery;
import plus.datacenter.core.entities.queries.queries.EqualQuery;
import plus.datacenter.core.entities.queries.queries.InQuery;
import plus.datacenter.core.entities.queries.queries.MatchQuery;

import java.util.Collection;

public class QueryUtils {

    public static Class<? extends Query> getQueryClass(QueryOperation operation) {
        switch (operation) {
            case EQUAL:
                return EqualQuery.class;
            case IN:
                return InQuery.class;
            case BETWEEN:
                return BetweenQuery.class;
            case MATCH:
                return MatchQuery.class;
            default:
                return EqualQuery.class;
        }
    }

    public static Class<?> getQueryValueClass(QueryOperation operation) {
        switch (operation) {
            case EQUAL:
                return Object.class;
            case IN:
                return Collection.class;
            case BETWEEN:
                return Rangeable.class;
            case MATCH:
                return String.class;
            default:
                return Object.class;
        }
    }

    public static QueryOperation getQueryOperation(String operation) {
        if (operation == null)
            return QueryOperation.EQUAL;
        switch (operation.toUpperCase()) {
            case "EQUAL":
                return QueryOperation.EQUAL;
            case "IN":
                return QueryOperation.IN;
            case "MATCH":
                return QueryOperation.MATCH;
            case "BETWEEN":
                return QueryOperation.BETWEEN;
            default:
                return QueryOperation.EQUAL;
        }
    }
}
