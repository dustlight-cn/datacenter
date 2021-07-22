package plus.datacenter.core.entities.queries.queries;

import io.swagger.v3.oas.annotations.media.Schema;
import plus.datacenter.core.entities.queries.Query;
import plus.datacenter.core.entities.queries.QueryOperation;

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
