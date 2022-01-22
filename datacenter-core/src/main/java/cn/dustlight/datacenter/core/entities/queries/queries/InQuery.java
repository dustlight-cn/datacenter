package cn.dustlight.datacenter.core.entities.queries.queries;

import cn.dustlight.datacenter.core.entities.queries.Query;
import cn.dustlight.datacenter.core.entities.queries.QueryOperation;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collection;

public class InQuery extends Query<Collection> {

    @Schema(defaultValue = "IN")
    @Override
    public QueryOperation getOpt() {
        return QueryOperation.IN;
    }

    @Override
    public void setOpt(QueryOperation opt) {
        super.setOpt(QueryOperation.IN);
    }
}
