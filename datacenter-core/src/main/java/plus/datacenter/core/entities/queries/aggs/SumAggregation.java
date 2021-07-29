package plus.datacenter.core.entities.queries.aggs;

import io.swagger.v3.oas.annotations.media.Schema;
import plus.datacenter.core.entities.queries.Aggregation;
import plus.datacenter.core.entities.queries.AggregationOperation;

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
