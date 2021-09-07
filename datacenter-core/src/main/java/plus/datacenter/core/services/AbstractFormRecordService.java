package plus.datacenter.core.services;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.FormRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public abstract class AbstractFormRecordService implements FormRecordService {

    @Getter
    private Collection<RecordEventHandler> joins = new HashSet<>();

    @Getter
    @Setter
    private RecordValidator recordValidator;

    @Override
    public Mono<FormRecord> createRecord(FormRecord origin, String clientId) {
        beforeCreate(origin, clientId, Instant.now());
        return doInsert(Arrays.asList(origin))
                .singleOrEmpty()
                .switchIfEmpty(Mono.error(ErrorEnum.CREATE_RECORD_FAILED.getException()));
    }

    @Override
    public Flux<FormRecord> createRecords(Collection<FormRecord> origin, String clientId) {
        Instant now = Instant.now();
        for (FormRecord record : origin) {
            beforeCreate(record, clientId, now);
        }
        return doInsert(origin);
    }

    @Override
    public Mono<FormRecord> getRecord(String id, String clientId) {
        return doGet(Arrays.asList(id), clientId)
                .singleOrEmpty()
                .switchIfEmpty(Mono.error(ErrorEnum.RECORD_NOT_FOUND.getException()));
    }

    @Override
    public Flux<FormRecord> getRecords(Collection<String> id, String clientId) {
        return doGet(id, clientId);
    }

    @Override
    public Mono<Void> updateRecord(FormRecord target, String clientId) {
        beforeUpdate(target, clientId, Instant.now());
        return doUpdate(Arrays.asList(target.getId()), target);
    }

    @Override
    public Mono<Void> updateRecords(Collection<String> ids, FormRecord target, String clientId) {
        beforeUpdate(target, clientId, Instant.now());
        return doUpdate(ids, target);
    }

    @Override
    public Mono<Void> deleteRecord(String id, String clientId) {
        return doDelete(Arrays.asList(id), clientId);
    }

    @Override
    public Mono<Void> deleteRecords(Collection<String> ids, String clientId) {
        return doDelete(ids, clientId);
    }

    protected abstract Flux<FormRecord> doInsert(Collection<FormRecord> records);

    protected abstract Flux<FormRecord> doGet(Collection<String> ids, String clientId);

    protected abstract Mono<Void> doUpdate(Collection<String> ids, FormRecord record);

    protected abstract Mono<Void> doDelete(Collection<String> ids, String clientId);

    /**
     * 执行事件
     *
     * @param records
     * @param eventType
     * @return
     */
    protected Flux<FormRecord> joinHandler(Flux<FormRecord> records, RecordEventHandler.EventType eventType) {
        Mono<List<FormRecord>> result = records.collectList();
        for (RecordEventHandler handler : joins) {
            result.flatMap(recordz -> handler.onEvent(recordz, eventType));
        }
        return result.flatMapMany(recordz -> Flux.fromIterable(recordz));
    }

    protected void beforeCreate(FormRecord record, String clientId, Instant now) {
        if (record == null) // 创建对象不可为空
            throw ErrorEnum.CREATE_RECORD_FAILED.details("Record can not be null").getException();
        if (!StringUtils.hasText(record.getFormId())) // 记录的表单 ID 不可为空
            throw ErrorEnum.CREATE_RECORD_FAILED.details("Form ID must be set").getException();
        if (!StringUtils.hasText(clientId)) // Client ID 不可为空
            throw ErrorEnum.CREATE_RECORD_FAILED.details("Client ID must be set").getException();

        // 设置创建时间与更新时间
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        // 设置 Client ID
        record.setClientId(clientId);
    }

    protected void beforeUpdate(FormRecord record, String clientId, Instant now) {
        if (record == null) // 创建对象不可为空
            throw ErrorEnum.CREATE_RECORD_FAILED.details("Record can not be null").getException();
        if (!StringUtils.hasText(record.getId())) // 记录的 ID 不可为空
            throw ErrorEnum.UPDATE_RECORD_FAILED.details("Record ID must be set").getException();
        if (!StringUtils.hasText(record.getFormId())) // 记录的表单 ID 不可为空
            throw ErrorEnum.CREATE_RECORD_FAILED.details("Form ID must be set").getException();
        if (!StringUtils.hasText(clientId)) // Client ID 不可为空
            throw ErrorEnum.CREATE_RECORD_FAILED.details("Client ID must be set").getException();

        // 设置更新时间
        record.setUpdatedAt(now);

        // 设置 Client ID
        record.setClientId(clientId);
    }

    public void addEventHandler(RecordEventHandler handler) {
        this.joins.add(handler);
    }

    public void removeEventHandler(RecordEventHandler handler) {
        this.joins.remove(handler);
    }
}
