package plus.datacenter.mongo.services;

import com.mongodb.reactivestreams.client.ClientSession;
import com.mongodb.reactivestreams.client.MongoClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.services.AbstractRecordService;
import plus.datacenter.core.services.RecordEventHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * 基于 MongoDB 实现的记录服务，支持事务。
 */
@Getter
@Setter
@AllArgsConstructor
public class MongoRecordService extends AbstractRecordService implements InitializingBean {

    private MongoClient mongoClient;
    private ReactiveMongoOperations operations;

    /**
     * 记录集合名称
     */
    private String collectionName;

    @Override
    protected Flux<Record> doInsert(Collection<Record> records) {
        return startTransaction() // 开启会话与事务
                .flatMapMany(clientSession -> operations.withSession(clientSession)
                        .insert(records, collectionName) // 插入数据
                        .collectList()
                        .flatMap(recordz -> joinHandler(recordz, RecordEventHandler.EventType.CREATE)) // 执行 Handler
                        .onErrorMap(throwable -> ErrorEnum.CREATE_RECORD_FAILED.details(throwable.getMessage()).getException()) // 转换异常类
                        .onErrorResume(throwable -> abortTransaction(throwable, clientSession)) // 回滚事务
                        .flatMapMany(recordz -> commitTransaction(recordz, clientSession)) // 提交事务
                        .doFinally(signalType -> clientSession.close())); // 结束会话
    }

    @Override
    protected Flux<Record> doGet(Collection<String> ids, String clientId) {
        return operations.find(Query.query(Criteria.where("clientId").is(clientId).and("_id").in(ids)), Record.class, collectionName);
    }

    @Override
    protected Mono<Void> doUpdate(Collection<String> ids, Record record) {
        Update update = new Update();

        update.set("updatedAt", record.getUpdatedAt()); // 设置更新时间

        if (StringUtils.hasText(record.getFormId()))
            update.set("formId", record.getFormId()); // 设置表单 ID（若不为空）
        if (StringUtils.hasText(record.getFormName()))
            update.set("formName", record.getFormName()); // 设置表单名（若不为空）
        if (record.getFormVersion() != null)
            update.set("formVersion", record.getFormVersion()); // 设置表单版本（若不为空）

        Map<String, Object> data = record.getData();
        if (data != null) { // 若更新数据不为空
            Iterator<Map.Entry<String, Object>> iterator = data.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> kv = iterator.next();
                if (kv.getValue() != null)
                    update.set("data." + kv.getKey(), kv.getValue()); // 设置更新数据
            }
        }

        return startTransaction()
                .flatMap(clientSession -> operations.withSession(clientSession)
                        .updateMulti(Query.query(Criteria.where("clientId").is(record.getClientId()).and("_id").in(ids)),
                                update,
                                Record.class, collectionName) // 更新数据
                        .flatMap(updateResult ->
                                buildRecords(record, ids, null)
                                        .flatMap(records -> joinHandler(records, RecordEventHandler.EventType.UPDATE))
                                        .map(records -> updateResult)) // 执行 Handler
                        .flatMap(updateResult -> updateResult.getMatchedCount() == ids.size() ?
                                commitTransaction(clientSession) : // 提交事务
                                Mono.error(new DatacenterException())) // 判断更新数量是否一致
                        .onErrorMap(throwable -> ErrorEnum.UPDATE_RECORD_FAILED.details(throwable.getMessage()).getException()) // 转换异常类
                        .onErrorResume(throwable -> abortTransaction(throwable, clientSession).then()) // 回滚事务
                        .doFinally(signalType -> clientSession.close()) // 结束会话
                );
    }

    @Override
    protected Mono<Void> doDelete(Collection<String> ids, String clientId) {
        return startTransaction()
                .flatMap(clientSession -> operations.withSession(clientSession)
                        .remove(Query.query(Criteria.where("clientId").is(clientId).and("_id").in(ids)), collectionName) // 删除数据
                        .flatMap(deleteResult -> buildRecords(null, ids, clientId)
                                .flatMap(records -> joinHandler(records, RecordEventHandler.EventType.DELETE))
                                .map(records -> deleteResult)) // 执行 Handler
                        .flatMap(deleteResult -> deleteResult.getDeletedCount() == ids.size() ?
                                commitTransaction(clientSession) : // 提交事务
                                Mono.error(new DatacenterException())) // 判断删除数量是否一致
                        .onErrorMap(throwable -> ErrorEnum.DELETE_RECORD_FAILED.details(throwable.getMessage()).getException()) // 转换异常类
                        .onErrorResume(throwable -> abortTransaction(throwable, clientSession).then()) // 回滚事务
                        .doFinally(signalType -> clientSession.close()) // 结束会话
                );
    }

    /**
     * 开启事务
     *
     * @return 启用了事务的会话对象
     */
    protected Mono<ClientSession> startTransaction() {
        return Mono.from(mongoClient.startSession())
                .flatMap(clientSession -> {
                    clientSession.startTransaction();
                    return Mono.just(clientSession);
                });
    }

    /**
     * 提交事务
     *
     * @param obj           提交完毕后将此集合以 Flux 形式返回
     * @param clientSession 会话对象
     * @param <T>           集合元素类型
     * @return
     */
    protected <T> Flux<T> commitTransaction(Collection<T> obj, ClientSession clientSession) {
        return Mono.from(clientSession.commitTransaction()).thenMany(Flux.fromIterable(obj));
    }

    /**
     * 提交事务
     *
     * @param clientSession 会话对象
     * @return
     */
    protected Mono<Void> commitTransaction(ClientSession clientSession) {
        return Mono.from(clientSession.commitTransaction()).then();
    }

    /**
     * 回滚事务
     *
     * @param throwable     异常对象
     * @param clientSession 会话对象
     * @param <T>
     * @return
     */
    protected <T> Mono<T> abortTransaction(Throwable throwable, ClientSession clientSession) {
        return Mono.from(clientSession.abortTransaction()).then(Mono.error(throwable));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(collectionName, "Record collection name must be set"); // 检查记录集合的名称是否为空
    }

    /**
     * 将一个记录对象根据 ID 集合进行复制
     *
     * @param origin   源对象
     * @param ids      ID 集合
     * @param clientId 客户端ID
     * @return
     */
    private static Mono<Collection<Record>> buildRecords(Record origin, Collection<String> ids, String clientId) {
        if (ids == null || ids.size() == 0)
            return Mono.empty();
        if (origin == null)
            origin = new Record();
        if (!StringUtils.hasText(origin.getClientId()) && StringUtils.hasText(clientId))
            origin.setClientId(clientId);
        Collection<Record> records = new ArrayList<>(ids.size());
        for (String id : ids) {
            Record r = null;
            try {
                r = (Record) origin.clone();
            } catch (CloneNotSupportedException e) {
                return Mono.error(e);
            }
            r.setId(id);
            records.add(r);
        }
        return Mono.just(records);
    }
}
