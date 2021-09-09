package plus.datacenter.core.services;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.Record;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;

/**
 * 抽象记录服务，抽象出四个批量增删改查方法。
 * <p>
 * 维护了一个 RecordEventHandler 列表，子类在进行增删改时调用 joinHandler 进行事件通知。若发生异常且支持事务则可以进行回滚。
 * <p>
 * 在进行增删改查之前，将使用 RecordValidator 进行校验。
 */
public abstract class AbstractRecordService implements RecordService, ApplicationContextAware, InitializingBean {

    @Getter
    private List<RecordEventHandler> joins = new ArrayList<>();

    @Getter
    private List<RecordValidator> validators = new ArrayList<>();

    @Getter
    @Setter
    private PrincipalHolder principalHolder;

    private ApplicationContext applicationContext;

    @Override
    public Mono<Record> createRecord(Record origin, String clientId) {
        return joinValidator(Arrays.asList(origin), clientId)
                .map(records -> {
                    if (records.size() == 0)
                        return records;
                    beforeCreate(records.iterator().next(), clientId, Instant.now());
                    return records;
                })
                .flatMap(records -> doInsert(records)
                        .singleOrEmpty()
                        .switchIfEmpty(Mono.error(ErrorEnum.CREATE_RECORD_FAILED.getException()))
                );
    }

    @Override
    public Flux<Record> createRecords(Collection<Record> origin, String clientId) {
        return joinValidator(origin, clientId)
                .map(records -> {
                    Instant now = Instant.now();
                    for (Record record : records) {
                        beforeCreate(record, clientId, now);
                    }
                    return records;
                })
                .flatMapMany(records -> doInsert(records));
    }

    @Override
    public Mono<Record> getRecord(String id, String clientId) {
        return doGet(Arrays.asList(id), clientId)
                .singleOrEmpty()
                .switchIfEmpty(Mono.error(ErrorEnum.RECORD_NOT_FOUND.getException()));
    }

    @Override
    public Flux<Record> getRecords(Collection<String> id, String clientId) {
        return doGet(id, clientId);
    }

    @Override
    public Mono<Void> updateRecord(Record target, String clientId) {
        return updateRecords(Arrays.asList(target.getId()), target, clientId);
    }

    @Override
    public Mono<Void> updateRecords(Collection<String> ids, Record target, String clientId) {
        return doGet(ids, clientId)
                .collectList()
                .flatMap(records -> {
                    if (records == null || records.size() == 0)
                        return Mono.empty();

                    String formName = null;
                    for (Record record : records) {
                        if (formName == null)
                            formName = record.getFormName();
                        else if (!formName.equals(record.getFormName()))
                            return Mono.error(ErrorEnum.UPDATE_RECORD_FAILED.details("Record's form name must be same").getException());
                    }
                    if (formName == null)
                        return Mono.error(ErrorEnum.UPDATE_RECORD_FAILED.details("All of Record's form name is empty or null").getException());
                    target.setFormId(null);
                    target.setFormName(formName);
                    return joinValidator(Arrays.asList(target), clientId)
                            .flatMap(recordz -> {
                                if (recordz == null || recordz.size() == 0)
                                    return Mono.error(ErrorEnum.UPDATE_RECORD_FAILED.getException());
                                Record record = recordz.iterator().next();
                                beforeUpdate(record, clientId, Instant.now());
                                return Mono.just(record);
                            })
                            .flatMap(record -> doUpdate(ids, record));
                });
    }

    @Override
    public Mono<Void> deleteRecord(String id, String clientId) {
        return doDelete(Arrays.asList(id), clientId);
    }

    @Override
    public Mono<Void> deleteRecords(Collection<String> ids, String clientId) {
        return doDelete(ids, clientId);
    }

    protected abstract Flux<Record> doInsert(Collection<Record> records);

    protected abstract Flux<Record> doGet(Collection<String> ids, String clientId);

    protected abstract Mono<Void> doUpdate(Collection<String> ids, Record record);

    protected abstract Mono<Void> doDelete(Collection<String> ids, String clientId);

    /**
     * 执行事件
     *
     * @param records
     * @param eventType
     * @return
     */
    protected Mono<Collection<Record>> joinHandler(Collection<Record> records, RecordEventHandler.EventType eventType) {
        Mono<Collection<Record>> result = Mono.just(records);
        for (RecordEventHandler handler : joins) {
            result = result.flatMap(recordz -> handler.onEvent(recordz, eventType));
        }
        return result;
    }

    /**
     * 执行校验
     *
     * @param records
     * @param clientId
     * @return
     */
    private Mono<Collection<Record>> joinValidator(Collection<Record> records, String clientId) {
        if (validators == null || validators.size() == 0)
            return Mono.just(records);
        if (principalHolder == null)
            return Mono.error(new DatacenterException("PrincipalHolder must be set"));
        return principalHolder.getPrincipal()
                .switchIfEmpty(Mono.error(new DatacenterException("principal is null")))
                .flatMap(principal -> {
                    Mono<Collection<Record>> result = Mono.just(records);
                    for (RecordValidator validator : validators) {
                        result = result.flatMap(recordz -> validator.validate(recordz, principal, clientId));
                    }
                    return result;
                });
    }

    protected void beforeCreate(Record record, String clientId, Instant now) {
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

    protected void beforeUpdate(Record record, String clientId, Instant now) {
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

    public void addEventHandler(RecordEventHandler... handler) {
        this.joins.addAll(Arrays.asList(handler));
        this.joins.sort(Comparator.comparingInt(Ordered::getOrder));
    }

    public void removeEventHandler(RecordEventHandler... handler) {
        this.joins.remove(Arrays.asList(handler));
    }

    public void addEventHandler(Collection<RecordEventHandler> handlers) {
        this.joins.addAll(handlers);
        this.joins.sort(Comparator.comparingInt(Ordered::getOrder));
    }

    public void removeEventHandler(Collection<RecordEventHandler> handler) {
        this.joins.remove(Arrays.asList(handler));
    }


    public void addValidator(RecordValidator... validator) {
        this.validators.addAll(Arrays.asList(validator));
        this.validators.sort(Comparator.comparingInt(Ordered::getOrder));
    }

    public void removeValidator(RecordValidator... validator) {
        this.validators.remove(Arrays.asList(validator));
    }

    public void addValidator(Collection<RecordValidator> validators) {
        this.validators.addAll(validators);
        this.validators.sort(Comparator.comparingInt(Ordered::getOrder));
    }

    public void removeValidator(Collection<RecordValidator> validators) {
        this.validators.remove(Arrays.asList(validators));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.joins == null || this.joins.size() == 0) {
            addEventHandler(applicationContext.getBeansOfType(RecordEventHandler.class).values());
        }
        if (this.validators == null || this.validators.size() == 0) {
            addValidator(applicationContext.getBeansOfType(RecordValidator.class).values());
        }
    }
}
