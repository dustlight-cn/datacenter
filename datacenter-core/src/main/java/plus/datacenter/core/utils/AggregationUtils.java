package plus.datacenter.core.utils;

import plus.datacenter.core.entities.queries.Aggregation;
import plus.datacenter.core.entities.queries.AggregationOperation;
import plus.datacenter.core.entities.queries.aggs.*;

public class AggregationUtils {

    public static Class<? extends Aggregation> getAggregationClass(AggregationOperation operation) {
        switch (operation) {
            case MAX:
                return MaxAggregation.class;
            case MIN:
                return MinAggregation.class;
            case SUM:
                return SumAggregation.class;
            case COUNT:
                return CountAggregation.class;
            case AVG:
                return AvgAggregation.class;
            case HISTOGRAM:
                return HistogramAggregation.class;
            case DATE_HISTOGRAM:
                return DateHistogramAggregation.class;
            case TERM:
            default:
                return TermAggregation.class;
        }
    }

    public static AggregationOperation getAggregationOperation(String operation) {
        if (operation == null)
            return AggregationOperation.TERM;
        switch (operation.toUpperCase()) {
            case "MAX":
                return AggregationOperation.MAX;
            case "MIN":
                return AggregationOperation.MIN;
            case "SUM":
                return AggregationOperation.SUM;
            case "COUNT":
                return AggregationOperation.COUNT;
            case "AVG":
                return AggregationOperation.AVG;
            case "HISTOGRAM":
                return AggregationOperation.HISTOGRAM;
            case "DATE_HISTOGRAM":
                return AggregationOperation.DATE_HISTOGRAM;
            case "TERM":
            default:
                return AggregationOperation.TERM;
        }
    }
}
