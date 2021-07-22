package plus.datacenter.core.entities.queries.queries;

import io.swagger.v3.oas.annotations.media.Schema;
import plus.datacenter.core.entities.queries.Query;
import plus.datacenter.core.entities.queries.QueryOperation;

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
