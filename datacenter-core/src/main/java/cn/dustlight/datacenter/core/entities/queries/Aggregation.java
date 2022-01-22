package cn.dustlight.datacenter.core.entities.queries;

import cn.dustlight.datacenter.core.entities.queries.aggs.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import cn.dustlight.datacenter.core.utils.AggregationDeserializer;

import java.io.Serializable;

@Schema(oneOf = {
        AvgAggregation.class,
        CountAggregation.class,
        DateHistogramAggregation.class,
        HistogramAggregation.class,
        MaxAggregation.class,
        MinAggregation.class,
        SumAggregation.class,
        TermAggregation.class
})
@Getter
@Setter
@JsonDeserialize(using = AggregationDeserializer.class)
public class Aggregation implements Serializable {

    private String name;
    private AggregationOperation opt;
    private String field;

    private Aggregation subAgg;
}
