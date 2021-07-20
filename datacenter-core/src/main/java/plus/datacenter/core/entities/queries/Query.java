package plus.datacenter.core.entities.queries;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.queries.queries.BetweenQuery;
import plus.datacenter.core.entities.queries.queries.EqualQuery;
import plus.datacenter.core.entities.queries.queries.InQuery;
import plus.datacenter.core.entities.queries.queries.MatchQuery;
import plus.datacenter.core.utils.QueryDeserializer;


@Schema(oneOf = {
        EqualQuery.class,
        InQuery.class,
        BetweenQuery.class,
        MatchQuery.class
})
@Getter
@Setter
@JsonDeserialize(using = QueryDeserializer.class)
public class Query<T> {

    private String name;
    private QueryOperation opt;
    private T value;

}
