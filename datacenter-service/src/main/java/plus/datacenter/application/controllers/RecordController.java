package plus.datacenter.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
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
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;
import plus.datacenter.core.entities.queries.Aggregation;
import plus.datacenter.core.entities.queries.Query;
import plus.datacenter.core.entities.queries.QueryOperation;
import plus.datacenter.core.entities.queries.queries.MatchQuery;
import plus.datacenter.core.services.RecordSearcher;
import plus.datacenter.core.services.RecordService;
import plus.datacenter.core.services.FormService;
import plus.datacenter.core.utils.FormUtils;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.*;

@Tag(name = "Records", description = "表单记录")
@RestController
@RequestMapping("/v1/")
@SecurityRequirement(name = "auth")
@CrossOrigin
public class RecordController {

    @Autowired
    private FormService formService;

    @Autowired
    private RecordService recordService;

    @Autowired
    private RecordSearcher recordSearcher;

    @PostMapping("record")
    @Operation(summary = "创建表单记录", description = "提交一条表单记录。")
    public Mono<Record> createRecord(@RequestBody Record record,
                                     @RequestParam(name = "cid", required = false) String clientId,
                                     ReactiveAuthClient reactiveAuthClient,
                                     AuthPrincipal principal) {
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> {
                    record.setFormId(null);
                    record.setFormVersion(null);

                    if (principal.getUid() != null)
                        record.setOwner(principal.getUidString());

                    return validate(record, principal, cid)
                            .flatMap(record1 -> recordService.createRecord(record1, cid))
                            .onErrorMap(throwable -> throwable instanceof DatacenterException ? throwable : ErrorEnum.CREATE_RESOURCE_FAILED.details(throwable.getMessage()).getException());
                });

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
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> recordService.getRecord(id, cid)
                        .flatMap(record1 -> {
                            record.setId(record1.getId());
                            record.setFormId(null);
                            record.setFormName(record1.getFormName());
                            return validate(record, principal, cid);
                        })
                        .flatMap(record1 -> {
                            record.setId(id);
                            record1.setCreatedAt(null);
                            record1.setOwner(null);
//                    record1.setFormId(null);
//                    record1.setFormVersion(null);
//                    record1.setFormName(null);
                            return recordService.updateRecord(record1, cid);
                        })
                        .onErrorMap(throwable -> throwable instanceof DatacenterException ? throwable : ErrorEnum.UPDATE_RESOURCE_FAILED.details(throwable.getMessage()).getException()))
                ;
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
                                    MatchQuery q = new MatchQuery();
                                    StringBuilder builder = new StringBuilder();
                                    Collection<Item> items = form.getItems().values();
                                    for (Item item : items) {
                                        if (item.getType() != ItemType.STRING)
                                            continue;
                                        if (builder.length() > 0)
                                            builder.append(',');
                                        builder.append("data.");
                                        builder.append(item.getName());
                                    }
                                    q.setName(builder.toString());
                                    q.setOpt(QueryOperation.MATCH);
                                    q.setValue(query);
                                    Collection<Query> qs = new HashSet<>();
                                    if (queries != null)
                                        qs.addAll(queries);
                                    qs.add(q);
                                    return qs;
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

    /**
     * 校验表单
     *
     * @param record
     * @param authPrincipal
     * @return
     */
    protected Mono<Record> validate(Record record, AuthPrincipal authPrincipal, String clientId) {
        return (StringUtils.hasText(record.getFormId()) ?
                formService.getForm(record.getFormId(), clientId) : formService.getLatestForm(record.getFormName(), clientId))
                .map(form -> {
                    record.setClientId(clientId);
                    record.setOwner(authPrincipal.getUidString());
                    record.setFormId(form.getId());
                    record.setFormName(form.getName());
                    record.setFormVersion(form.getVersion());


                    Map<String, Item> items = form.getItems();
                    Map<String, Object> data = record.getData();
                    Map<String, Object> validatedData = new HashMap<>();

                    Iterator<Map.Entry<String, Item>> iterator = items.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Item> kv = iterator.next();
                        String key = kv.getKey();
                        Item item = kv.getValue();
                        if (item == null)
                            continue;
                        Object value = data == null ? null : data.get(key);

                        if (item.getArray() != null && item.getArray()) {
                            // Item 为数组
                            if (value == null) {
                                if (item.getRequired() != null && item.getRequired())
                                    throw new IllegalArgumentException("Validation failed: '" + key + "' is empty but required");
                                else
                                    continue;
                            }
                            if (!(value instanceof Collection))
                                throw new IllegalArgumentException("Validation failed: '" + key + "' require an array value");
                            Collection arrays = (Collection) value;
                            if ((arrays == null || arrays.size() == 0) && item.getRequired())
                                throw new IllegalArgumentException("Validation failed: '" + key + "' is empty but required");
                            ItemType t = item.getType();
                            int i = 0;
                            Collection<Object> transformedValues = new ArrayList<>();
                            for (Object v : arrays) {
                                v = FormUtils.transformItemValue(v, t);
                                if (!item.validate(v))
                                    throw new IllegalArgumentException("Validation failed: '" + key + "[" + i + "]'");
                                transformedValues.add(t == ItemType.FORM ? new ObjectId((String) v) : v);
                                i++;
                            }
                            validatedData.put(key, transformedValues);

                        } else {
                            // Item 不为数组
                            if (value instanceof Collection && item.getType() != ItemType.SELECT)
                                throw new IllegalArgumentException("Validation failed: '" + key + "' is not array");
                            value = FormUtils.transformItemValue(value, item.getType());
                            if (!item.validate(value))
                                throw new IllegalArgumentException("Validation failed: '" + key + "'");
                            validatedData.put(key, item.getType() == ItemType.FORM ? new ObjectId((String) value) : value);
                        }

                    }

                    record.setData(validatedData);

                    return record;
                });
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
