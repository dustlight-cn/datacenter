package plus.datacenter.core.entities.queries.aggs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.queries.Aggregation;
import plus.datacenter.core.entities.queries.AggregationOperation;

@Getter
@Setter
public class HistogramAggregation extends Aggregation {

    private double interval;

    @Schema(defaultValue = "HISTOGRAM")
    @Override
    public AggregationOperation getOpt() {
        return AggregationOperation.HISTOGRAM;
    }

    @Override
    public void setOpt(AggregationOperation opt) {
        super.setOpt(AggregationOperation.HISTOGRAM);
    }
}
