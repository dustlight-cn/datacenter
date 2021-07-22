package plus.datacenter.core.entities.queries.queries;

import io.swagger.v3.oas.annotations.media.Schema;
import plus.datacenter.core.entities.Rangeable;
import plus.datacenter.core.entities.queries.Query;
import plus.datacenter.core.entities.queries.QueryOperation;

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
