package cn.dustlight.datacenter.core.entities.queries.queries;

import cn.dustlight.datacenter.core.entities.queries.Query;
import cn.dustlight.datacenter.core.entities.queries.QueryOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.dustlight.datacenter.core.entities.Rangeable;

public class BetweenQuery extends Query<Rangeable> {

    @Schema(defaultValue = "BETWEEN")
    @Override
    public QueryOperation getOpt() {
        return QueryOperation.BETWEEN;
    }

    @Override
    public void setOpt(QueryOperation opt) {
        super.setOpt(QueryOperation.BETWEEN);
    }
}
