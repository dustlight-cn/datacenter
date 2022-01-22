package cn.dustlight.datacenter.core.entities.queries.aggs;

import cn.dustlight.datacenter.core.entities.queries.Aggregation;
import cn.dustlight.datacenter.core.entities.queries.AggregationOperation;
import io.swagger.v3.oas.annotations.media.Schema;

public class AvgAggregation extends Aggregation {

    @Schema(defaultValue = "AVG")
    @Override
    public AggregationOperation getOpt() {
        return AggregationOperation.AVG;
    }

    @Override
    public void setOpt(AggregationOperation opt) {
        super.setOpt(AggregationOperation.AVG);
    }
}
