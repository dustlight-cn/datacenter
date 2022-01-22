package cn.dustlight.datacenter.core.entities.queries.queries;

import cn.dustlight.datacenter.core.entities.queries.Query;
import cn.dustlight.datacenter.core.entities.queries.QueryOperation;
import io.swagger.v3.oas.annotations.media.Schema;

public class EqualQuery extends Query {

    @Schema(defaultValue = "EQUAL")
    @Override
    public QueryOperation getOpt() {
        return QueryOperation.EQUAL;
    }

    @Override
    public void setOpt(QueryOperation opt) {
        super.setOpt(QueryOperation.EQUAL);
    }
}
