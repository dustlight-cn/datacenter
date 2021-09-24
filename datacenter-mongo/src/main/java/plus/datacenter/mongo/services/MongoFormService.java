package plus.datacenter.mongo.services;

import com.mongodb.DuplicateKeyException;
import com.mongodb.reactivestreams.client.ClientSession;
import com.mongodb.reactivestreams.client.MongoClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.services.AbstractFormService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * 基于 MongoDB 实现的表单服务，支持事务。
 */
@Getter
@Setter
@AllArgsConstructor
public class MongoFormService extends AbstractFormService implements InitializingBean {

    private MongoClient client;
    private ReactiveMongoOperations operations;

    /**
     * 记录集合名称
     */
    private String collectionName;

    @Override
    protected Flux<Form> doInsert(Collection<Form> origins) {
        Map<String, FormMeta> metaMap = new HashMap<>();
        Map<String, Form> formMap = new HashMap<>();
        for (Form form : origins) {
            FormMeta formMeta = newFormMeta(form); // 新建 Meta
            form.setVersion(formMeta.getVersion()); // 设置版本号
            metaMap.put(form.getName(), formMeta);
            formMap.put(form.getName(), form);
        }
        return startTransaction()
                .flatMapMany(clientSession -> operations.withSession(clientSession)
                        .insert(formMap.values(), collectionName) // 插入表单数据
                        .collectList()
                        .flatMap(forms -> {
                            for (Form form : forms) {
                                FormMeta meta = metaMap.get(form.getName());
                                if (meta == null)
                                    return Mono.error(new DatacenterException());
                                meta.setCurrentId(form.getId()); // 更新表单 ID 到 Meta
                            }
                            Collection<FormMeta> metas = metaMap.values();
                            for (FormMeta meta : metas) {
                                if (!StringUtils.hasText(meta.getCurrentId())) // 检查是否有漏网之鱼
                                    return Mono.error(new DatacenterException());
                            }
                            return operations.insert(metas, getFormMetaCollectionName()) // 插入表单 Meta 数据
                                    .collectList()
                                    .map(formMetas -> forms);
                        })
                        .onErrorMap(throwable -> (throwable instanceof DuplicateKeyException) ?
                                ErrorEnum.FORM_EXISTS.details(throwable).getException() :
                                ErrorEnum.CREATE_FORM_FAILED.details(throwable).getException()) // 转换异常类
                        .onErrorResume(throwable -> abortTransaction(throwable, clientSession)) // 回滚事务
                        .flatMapMany(forms -> commitTransaction(forms, clientSession)) // 提交事务
                        .doFinally(signalType -> clientSession.close()) // 结束会话
                );
    }

    @Override
    protected Flux<Form> doGet(Collection<String> ids, String clientId) {
        return operations.find(Query.query(Criteria.where("clientId").is(clientId).and("_id").in(ids)),
                Form.class,
                collectionName);
    }

    @Override
    protected Flux<Form> doGetLatest(Collection<String> names, String clientId) {
        Set<String> ids = new HashSet<>();
        for (String name : names) {
            ids.add(getFormMetaId(clientId, name));
        }
        return operations.find(Query.query(Criteria.where("_id").in(ids).and("clientId").is(clientId)),
                        FormMeta.class,
                        getFormMetaCollectionName()) // 查询 Meta
                .collectList()
                .map(formMetas -> {
                    Set<String> formIds = new HashSet<>();
                    for (FormMeta meta : formMetas)
                        formIds.add(meta.getCurrentId());
                    return formIds; // 根据 Meta 获取到最新版本的表单 ID 集合。
                })
                .flatMapMany(formIds -> doGet(formIds, clientId)); // 根据表单 ID 集合获取表单对象集合。
    }

    @Override
    protected Flux<Form> doUpdate(Collection<Form> targets) {
        return startTransaction()
                .flatMapMany(clientSession -> {
                    Map<String, Form> formMap = new HashMap<>(); // 表单名与表单的映射，用于表单名去重
                    for (Form target : targets) {
                        formMap.put(target.getName(), target);
                    }

                    Collection<Form> forms = formMap.values(); // 去重后的表单对象
                    Map<String, Form> formIdMap = new HashMap<>(); // 表单 ID 与表单的映射

                    Flux<FormMeta> formMetaFlux = Flux.empty();
                    for (Form form : forms) {
                        form.setId(new ObjectId().toHexString()); // 生成 ObjectID
                        form.setVersion(null); // 置空版本
                        formIdMap.put(form.getId(), form); // 存入字典

                        Update update = new Update();
                        update.set("currentId", form.getId());
                        update.inc("version", 1);

                        formMetaFlux = formMetaFlux.transform(f -> f.concatWith(
                                operations.withSession(clientSession)
                                        .findAndModify(Query.query(Criteria.where("clientId").is(form.getClientId()).and("_id").is(getFormMetaId(form))),
                                                update,
                                                FormMeta.class,
                                                getFormMetaCollectionName()) // 更新 Meta 并获取旧数据
                                        .onErrorMap(throwable -> new DatacenterException(throwable.getMessage(), throwable))
                                        .switchIfEmpty(Mono.error(ErrorEnum.FORM_NOT_FOUND.details(form.getName()).getException()))
                                        .flatMap(formMeta -> Mono.fromRunnable(() -> formMeta.setCurrentId(form.getId()))
                                                .then(Mono.just(formMeta))) // 更新 currentId 以便后面查找
                        ));
                    }
                    return formMetaFlux.collectList()
                            .flatMapMany(formMetas -> {
                                for (FormMeta meta : formMetas) {
                                    Form form = formIdMap.get(meta.getCurrentId()); // 根据 Meta 的 Current Id 寻找表单
                                    if (form == null)
                                        return Mono.error(new DatacenterException());
                                    form.setVersion(meta.getVersion() + 1); // 版本号 +1
                                }
                                for (Form form : forms) {
                                    if (form.getVersion() == null)
                                        return Mono.error(new DatacenterException());
                                }
                                return operations.withSession(clientSession)
                                        .insert(forms, collectionName); // 插入新表单数据
                            })
                            .collectList()
                            .onErrorMap(throwable -> ErrorEnum.UPDATE_FORM_FAILED.details(throwable).getException()) // 转换异常类
                            .onErrorResume(throwable -> abortTransaction(throwable, clientSession)) // 回滚事务
                            .flatMapMany(forms1 -> commitTransaction(forms1, clientSession)) // 提交事务
                            .doFinally(signalType -> clientSession.close()); // 结束会话
                });
    }

    @Override
    protected Mono<Void> doDelete(Collection<String> names, String clientId) {
        Set<String> ids = new HashSet<>();
        for (String name : names) {
            ids.add(getFormMetaId(clientId, name)); // 去重
        }
        return startTransaction() // 开启事务
                .flatMap(clientSession -> operations.withSession(clientSession)
                        .remove(Query.query(Criteria.where("_id").in(ids)), FormMeta.class, getFormMetaCollectionName()) // 删除 Meta 数据
                        .flatMap(deleteResult -> deleteResult.getDeletedCount() != ids.size() ?
                                Mono.error(new DatacenterException()) :
                                Mono.just(deleteResult)) // 判断删除数量是否一致
                        .flatMap(deleteResult -> operations.withSession(clientSession)
                                .remove(Query.query(Criteria.where("name").in(names)), Form.class, collectionName)) // 再删除表单数据
                        .onErrorMap(throwable -> ErrorEnum.DELETE_FORM_FAILED.details(throwable).getException()) // 转换异常类
                        .onErrorResume(throwable -> abortTransaction(throwable, clientSession)) // 回滚事务
                        .flatMap(deleteResult -> commitTransaction(clientSession)) // 提交事务
                        .doFinally(signalType -> clientSession.close()) // 结束会话
                );
    }

    /**
     * 获取表单元信息集合名称
     *
     * @return
     */
    protected String getFormMetaCollectionName() {
        return collectionName + "_meta";
    }

    /**
     * 获取表单元信息 ID
     *
     * @param form
     * @return
     */
    protected String getFormMetaId(Form form) {
        if (form == null)
            throw new NullPointerException("Fail to get form meta id, cause form is null");
        return getFormMetaId(form.getClientId(), form.getName());
    }

    /**
     * 获取表单元信息 ID
     *
     * @param clientId
     * @param formName
     * @return
     */
    protected String getFormMetaId(String clientId, String formName) {
        if (!StringUtils.hasText(formName))
            throw new DatacenterException("Fail to get form meta id, cause formName is empty");
        if (!StringUtils.hasText(clientId))
            throw new DatacenterException("Fail to get form meta id, cause clientId is empty");
        return String.format("%s:%s", clientId, formName);
    }

    protected FormMeta newFormMeta(Form origin) {
        FormMeta meta = new FormMeta();
        meta.setId(getFormMetaId(origin));
        meta.setVersion(0);
        meta.setOwner(origin.getOwner());
        meta.setClientId(origin.getClientId());
        return meta;
    }

    /**
     * 开启事务
     *
     * @return 启用了事务的会话对象
     */
    protected Mono<ClientSession> startTransaction() {
        return Mono.from(client.startSession())
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
        Assert.hasText(collectionName, "Form collection name must be set");
    }


    /**
     * 表单元信息，用于记录当前版本号以及当前版本表单 ID
     */
    @Getter
    @Setter
    public static class FormMeta {

        @Id
        private String id;
        private String clientId;
        private String owner;
        private String currentId;

        /**
         * 表单最新版本号
         */
        private int version = 0;
    }
}
