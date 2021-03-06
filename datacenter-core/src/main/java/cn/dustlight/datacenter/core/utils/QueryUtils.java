package cn.dustlight.datacenter.core.utils;

import cn.dustlight.datacenter.core.entities.queries.Query;
import cn.dustlight.datacenter.core.entities.queries.QueryOperation;
import cn.dustlight.datacenter.core.entities.queries.queries.EqualQuery;
import cn.dustlight.datacenter.core.entities.Rangeable;
import cn.dustlight.datacenter.core.entities.queries.queries.BetweenQuery;
import cn.dustlight.datacenter.core.entities.queries.queries.InQuery;
import cn.dustlight.datacenter.core.entities.queries.queries.MatchQuery;

import java.util.Collection;

public class QueryUtils {

    public static Class<? extends Query> getQueryClass(QueryOperation operation) {
        switch (operation) {
            case IN:
                return InQuery.class;
            case BETWEEN:
                return BetweenQuery.class;
            case MATCH:
                return MatchQuery.class;
            case EQUAL:
            default:
                return EqualQuery.class;
        }
    }

    public static Class<?> getQueryValueClass(QueryOperation operation) {
        switch (operation) {
            case IN:
                return Collection.class;
            case BETWEEN:
                return Rangeable.class;
            case MATCH:
                return String.class;
            case EQUAL:
            default:
                return Object.class;
        }
    }

    public static QueryOperation getQueryOperation(String operation) {
        if (operation == null)
            return QueryOperation.EQUAL;
        switch (operation.toUpperCase()) {
            case "IN":
                return QueryOperation.IN;
            case "MATCH":
                return QueryOperation.MATCH;
            case "BETWEEN":
                return QueryOperation.BETWEEN;
            case "EQUAL":
            default:
                return QueryOperation.EQUAL;
        }
    }
}
