package plus.datacenter.application.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import plus.auth.client.reactive.ReactiveAuthClient;
import plus.auth.entities.QueryResult;
import plus.auth.resources.core.AuthPrincipal;
import plus.datacenter.application.ClientUtils;
import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.entities.queries.Aggregation;
import plus.datacenter.core.entities.queries.Query;
import plus.datacenter.core.entities.queries.QueryOperation;
import plus.datacenter.core.entities.queries.queries.MatchQuery;
import plus.datacenter.core.services.RecordSearcher;
import plus.datacenter.core.services.FormService;
import plus.datacenter.core.utils.FormUtils;
import plus.datacenter.mongo.services.MongoRecordService;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.*;

@Tag(name = "Records", description = "表单记录")
@RestController
@RequestMapping("/v1/")
@SecurityRequirement(name = "auth")
@CrossOrigin("*")
public class RecordController {

    @Autowired
    private FormService formService;

    @Autowired
    private MongoRecordService recordService;

    @Autowired
    private RecordSearcher recordSearcher;

    private static final Set<String> notStringFormat = Set.of("date-time", "date", "time");

    @PostMapping("record")
    @Operation(summary = "创建表单记录", description = "提交一条表单记录。")
    public Mono<Record> createRecord(@RequestBody Record record,
                                     @RequestParam(name = "cid", required = false) String clientId,
                                     ReactiveAuthClient reactiveAuthClient,
                                     AuthPrincipal principal) {
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> recordService.createRecord(record, cid)
                        .onErrorMap(throwable -> throwable instanceof DatacenterException ?
                                throwable :
                                ErrorEnum.CREATE_RESOURCE_FAILED.details(throwable).getException()));

    }

    @GetMapping("record/{id}")
    @Operation(summary = "获取表单记录", description = "获取一条表单记录。")
    public Mono<Record> getRecord(@PathVariable String id,
                                  @RequestParam(name = "cid", required = false) String clientId,
                                  ReactiveAuthClient reactiveAuthClient,
                                  AuthPrincipal principal) {
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> recordService.getRecord(id, cid));
    }

    @DeleteMapping("record/{id}")
    @Operation(summary = "删除表单记录", description = "删除一条表单记录。")
    public Mono<Void> deleteRecord(@PathVariable String id,
                                   @RequestParam(name = "cid", required = false) String clientId,
                                   ReactiveAuthClient reactiveAuthClient,
                                   AuthPrincipal principal) {
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> recordService.deleteRecord(id, cid));
    }

    @DeleteMapping("records")
    @Operation(summary = "批量删除表单记录", description = "根据 id 批量删除表单记录。")
    public Mono<Void> deleteRecords(@RequestBody Collection<String> ids,
                                    @RequestParam(name = "cid", required = false) String clientId,
                                    ReactiveAuthClient reactiveAuthClient,
                                    AuthPrincipal principal) {
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> recordService.deleteRecords(ids, cid));
    }

    @PutMapping("record/{id}")
    @Operation(summary = "更新表单记录", description = "更新一条表单记录。")
    public Mono<Void> updateRecord(@PathVariable String id,
                                   @RequestBody Record record,
                                   @RequestParam(name = "cid", required = false) String clientId,
                                   ReactiveAuthClient reactiveAuthClient,
                                   AuthPrincipal principal) {
        if (record == null)
            return Mono.error(ErrorEnum.UPDATE_RECORD_FAILED.getException());
        record.setId(id);
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> recordService.updateRecord(record, cid))
                .onErrorMap(throwable -> throwable instanceof DatacenterException ?
                        throwable :
                        ErrorEnum.UPDATE_RESOURCE_FAILED.details(throwable).getException()
                );
    }

    @PostMapping("records/queries")
    @Operation(summary = "检索表单记录", description = "列出或搜索表单记录。")
    public Mono<QueryResult<Record>> findRecords(@RequestParam @Parameter(description = "表单名称。") String name,
                                                 @RequestParam(required = false) @Parameter(description = "关键词，对表单的 STRING 类型进行全文搜索。") String query,
                                                 @RequestParam(required = false) @Parameter(description = "排序字段，如：update （正序排序） -update（倒序排序）。") List<String> orders,
                                                 @RequestParam(required = false, defaultValue = "0") int page,
                                                 @RequestParam(required = false, defaultValue = "10") int size,
                                                 @RequestBody(required = false) @Parameter(description = "过滤器。") Collection<Query> queries,
                                                 @RequestParam(name = "cid", required = false) String clientId,
                                                 ReactiveAuthClient reactiveAuthClient,
                                                 AuthPrincipal principal) {
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> {
                    if (StringUtils.hasText(query)) {
                        return formService.getLatestForm(name, cid)
                                .map(form -> {
                                    Set<String> stringFields = FormUtils.getFieldsByType(form, "string", node -> {
                                        JsonNode n;
                                        if (node == null ||
                                                !node.has("format") ||
                                                (n = node.get("format")) == null ||
                                                !(n instanceof TextNode))
                                            return true;
                                        return !notStringFormat.contains(n.asText());
                                    });
                                    if (stringFields != null && stringFields.size() > 0) {
                                        StringBuilder builder = new StringBuilder();
                                        for (String field : stringFields) {
                                            if (builder.length() > 0)
                                                builder.append(',');
                                            builder.append("data.");
                                            builder.append(field.replace('/', '.'));
                                        }

                                        MatchQuery q = new MatchQuery();
                                        q.setName(builder.toString());
                                        q.setOpt(QueryOperation.MATCH);
                                        q.setValue(query);
                                        Collection<Query> qs = new HashSet<>();
                                        if (queries != null)
                                            qs.addAll(queries);
                                        qs.add(q);
                                        return qs;
                                    }
                                    return queries;
                                })
                                .flatMap(qs -> recordSearcher.findRecord(cid,
                                        name,
                                        qs,
                                        orders,
                                        page,
                                        size));
                    }
                    return recordSearcher.findRecord(cid,
                            name,
                            queries,
                            orders,
                            page,
                            size);
                });
    }

    @Operation(summary = "聚合表单记录", description = "聚合查询表单记录。")
    @PostMapping("records/aggregations")
    public Mono<?> aggregate(@RequestParam @Parameter(description = "表单名称。") String name,
                             @RequestBody AggregationQuery query,
                             @RequestParam(name = "cid", required = false) String clientId,
                             ReactiveAuthClient reactiveAuthClient,
                             AuthPrincipal principal) {
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> recordSearcher.aggregate(cid, name, query.getFilter(), query.getAggs()));
    }

    @PostMapping("validation")
    @Operation(summary = "验证表单记录", description = "验证一条表单记录。")
    public Mono<Void> verify(@RequestBody Record record,
                             @RequestParam(name = "cid", required = false) String clientId,
                             ReactiveAuthClient reactiveAuthClient,
                             AuthPrincipal principal) {
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> recordService.verifyRecord(record, cid)
                        .onErrorMap(throwable -> throwable instanceof DatacenterException ?
                                throwable :
                                ErrorEnum.RECORD_INVALID.details(throwable).getException()));

    }

    @Getter
    @Setter
    public static class AggregationQuery implements Serializable {

        @Parameter(description = "聚合查询。")
        private Aggregation aggs;

        @Parameter(description = "过滤器。")
        private Collection<Query> filter;

    }
}
