package cn.dustlight.datacenter.core.entities.queries.aggs;

import cn.dustlight.datacenter.core.entities.queries.Aggregation;
import cn.dustlight.datacenter.core.entities.queries.AggregationOperation;
import io.swagger.v3.oas.annotations.media.Schema;

public class SumAggregation extends Aggregation {

    @Schema(defaultValue = "SUM")
    @Override
    public AggregationOperation getOpt() {
        return AggregationOperation.SUM;
    }

    @Override
    public void setOpt(AggregationOperation opt) {
        super.setOpt(AggregationOperation.SUM);
    }
}
