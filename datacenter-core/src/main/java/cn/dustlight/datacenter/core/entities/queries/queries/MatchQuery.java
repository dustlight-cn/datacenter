package cn.dustlight.datacenter.core.entities.queries.queries;

import cn.dustlight.datacenter.core.entities.queries.Query;
import cn.dustlight.datacenter.core.entities.queries.QueryOperation;
import io.swagger.v3.oas.annotations.media.Schema;

public class MatchQuery extends Query<String> {

    @Schema(defaultValue = "MATCH")
    @Override
    public QueryOperation getOpt() {
        return QueryOperation.MATCH;
    }

    @Override
    public void setOpt(QueryOperation opt) {
        super.setOpt(QueryOperation.MATCH);
    }
}
