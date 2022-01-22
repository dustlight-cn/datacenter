package cn.dustlight.datacenter.core.entities.queries.aggs;

import cn.dustlight.datacenter.core.entities.queries.Aggregation;
import cn.dustlight.datacenter.core.entities.queries.AggregationOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema
public class DateHistogramAggregation extends Aggregation {

    private int interval;
    private IntervalUnit unit = IntervalUnit.DAY;

    @Schema(defaultValue = "DATE_HISTOGRAM")
    @Override
    public AggregationOperation getOpt() {
        return AggregationOperation.DATE_HISTOGRAM;
    }

    @Override
    public void setOpt(AggregationOperation opt) {
        super.setOpt(AggregationOperation.DATE_HISTOGRAM);
    }

    public enum IntervalUnit {
        SECOND("s"),
        MINUTE("m"),
        HOUR("h"),
        DAY("d"),
        WEEK("w"),
        MONTH("M"),
        QUARTER("q"),
        YEAR("y");

        private String value;

        IntervalUnit(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
