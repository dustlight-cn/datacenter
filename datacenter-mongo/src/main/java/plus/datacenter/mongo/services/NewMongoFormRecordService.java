package plus.datacenter.mongo.services;

import com.mongodb.reactivestreams.client.ClientSession;
import com.mongodb.reactivestreams.client.MongoClient;
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
import plus.datacenter.core.entities.forms.FormRecord;
import plus.datacenter.core.services.AbstractFormRecordService;
import plus.datacenter.core.services.RecordEventHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@Getter
@Setter
public class NewMongoFormRecordService extends AbstractFormRecordService implements InitializingBean {

    private MongoClient mongoClient;
    private ReactiveMongoOperations operations;

    private String collectionName;

    @Override
    protected Flux<FormRecord> doInsert(Collection<FormRecord> records) {
        return startTransaction() // 开启会话与事务
                .flatMapMany(clientSession -> operations.withSession(clientSession)
                        .insert(records, collectionName) // 插入数据
                        .transform(recordz -> joinHandler(recordz, RecordEventHandler.EventType.CREATE)) // 执行 Handler
                        .transform(recordz -> commitTransaction(recordz, clientSession)) // 提交事务
                        .onErrorMap(throwable -> ErrorEnum.CREATE_RECORD_FAILED.details(throwable.getMessage()).getException()) // 转换异常类
                        .onErrorResume(throwable -> abortTransaction(throwable, clientSession)) // 回滚事务
                        .doFinally(signalType -> clientSession.close())); // 结束会话
    }

    @Override
    protected Flux<FormRecord> doGet(Collection<String> ids, String clientId) {
        return operations.find(Query.query(Criteria.where("clientId").is(clientId).and("_id").in(ids)), FormRecord.class, collectionName);
    }

    @Override
    protected Mono<Void> doUpdate(Collection<String> ids, FormRecord record) {
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
                                FormRecord.class, collectionName) // 更新数据
                        .flatMap(updateResult -> updateResult.getMatchedCount() == ids.size() ?
                                Mono.empty() :
                                Mono.error(new DatacenterException())) // 判断更新数量是否一致
                        .flatMap(unused -> commitTransaction(clientSession)) // 提交事务
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
                        .flatMap(deleteResult -> deleteResult.getDeletedCount() == ids.size() ?
                                Mono.empty() :
                                Mono.error(new DatacenterException())) // 判断删除数量是否一致
                        .flatMap(unused -> commitTransaction(clientSession)) // 提交事务
                        .onErrorMap(throwable -> ErrorEnum.DELETE_RECORD_FAILED.details(throwable.getMessage()).getException()) // 转换异常类
                        .onErrorResume(throwable -> abortTransaction(throwable, clientSession).then()) // 回滚事务
                        .doFinally(signalType -> clientSession.close()) // 结束会话
                );
    }

    protected Mono<ClientSession> startTransaction() {
        return Mono.from(mongoClient.startSession())
                .flatMap(clientSession -> {
                    clientSession.startTransaction();
                    return Mono.just(clientSession);
                });
    }

    protected <T> Flux<T> commitTransaction(Flux<T> obj, ClientSession clientSession) {
        return Mono.from(clientSession.commitTransaction()).thenMany(obj);
    }

    protected Mono<Void> commitTransaction(ClientSession clientSession) {
        return Mono.from(clientSession.commitTransaction()).then();
    }

    protected <T> Flux<T> abortTransaction(Throwable throwable, ClientSession clientSession) {
        return Mono.from(clientSession.abortTransaction()).thenMany(Mono.error(throwable));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(collectionName, "Record collection name must be set");
    }
}
