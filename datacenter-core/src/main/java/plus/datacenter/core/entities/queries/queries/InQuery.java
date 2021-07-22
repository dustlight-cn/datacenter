package plus.datacenter.core.entities.queries.queries;

import io.swagger.v3.oas.annotations.media.Schema;
import plus.datacenter.core.entities.queries.Query;
import plus.datacenter.core.entities.queries.QueryOperation;

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
