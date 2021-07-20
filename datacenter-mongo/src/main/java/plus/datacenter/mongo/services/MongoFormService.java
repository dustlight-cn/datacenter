package plus.datacenter.mongo.services;

import com.mongodb.reactivestreams.client.MongoClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import plus.auth.entities.QueryResult;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.services.FormService;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;

@Getter
@Setter
@AllArgsConstructor
public class MongoFormService implements FormService {

    private MongoClient client;
    private ReactiveMongoOperations operations;
    private String collectionName;

    @Transactional
    @Override
    public Mono<Form> createForm(Form origin) {
        if (!StringUtils.hasText(origin.getName()))
            ErrorEnum.CREATE_FORM_FAILED.details("form name can't not be null!").throwException();
        FormMeta meta = new FormMeta();
        meta.setName(getMetaName(origin));
        meta.setVersion(0);
        meta.setOwner(origin.getOwner());
        meta.setClientId(origin.getClientId());

        origin.setId(null);
        Instant t = Instant.now();
        origin.setVersion(meta.version);
        origin.setCreatedAt(t);

        return Mono.from(client.startSession())
                .flatMap(clientSession -> {
                            clientSession.startTransaction();
                            ReactiveMongoOperations op = operations.withSession(clientSession);
                            return op.withSession(clientSession)
                                    .insert(origin, collectionName)
                                    .onErrorMap(throwable -> ErrorEnum.CREATE_FORM_FAILED.details(throwable.getMessage()).getException())
                                    .flatMap(form -> {
                                        meta.setCurrentId(form.getId());
                                        return op.insert(meta, getMetaCollectionName())
                                                .onErrorMap(throwable -> (throwable instanceof DuplicateKeyException) ?
                                                        ErrorEnum.FORM_EXISTS.details(throwable.getMessage()).getException() :
                                                        ErrorEnum.CREATE_FORM_FAILED.details(throwable.getMessage()).getException())
                                                .map(formMeta -> form);
                                    })
                                    .onErrorResume(throwable -> Mono.from(clientSession.abortTransaction()).then(Mono.error(throwable)))
                                    .flatMap(val -> Mono.from(clientSession.commitTransaction()).then(Mono.just(val)))
                                    .doFinally(signalType -> clientSession.close());
                        }
                );
    }

    @Override
    public Mono<Form> updateForm(Form target) {
        ObjectId id = new ObjectId();
        Update update = new Update();
        update.set("currentId", id.toHexString());
        update.inc("version", 1);
        if (target.getOwner() != null)
            update.set("owner", target.getOwner());

        return Mono.from(client.startSession())
                .flatMap(clientSession -> {
                    clientSession.startTransaction();
                    ReactiveMongoOperations op = operations.withSession(clientSession);
                    return op
                            .findAndModify(Query.query(Criteria.where("_id").is(getMetaName(target)).and("clientId").is(target.getClientId())),
                                    update,
                                    FormMeta.class,
                                    getMetaCollectionName())
                            .onErrorMap(throwable -> ErrorEnum.UPDATE_FORM_FAILED.details(throwable.getMessage()).getException())
                            .switchIfEmpty(Mono.error(ErrorEnum.FORM_NOT_FOUND.getException()))
                            .flatMap(formMeta -> {
                                target.setId(id.toHexString());
                                target.setCreatedAt(Instant.now());
                                target.setVersion(formMeta.version + 1);
                                return op
                                        .insert(target, collectionName)
                                        .onErrorMap(throwable -> ErrorEnum.UPDATE_FORM_FAILED.details(throwable.getMessage()).getException());
                            })
                            .onErrorResume(throwable -> Mono.from(clientSession.abortTransaction()).then(Mono.error(throwable)))
                            .flatMap(val -> Mono.from(clientSession.commitTransaction()).then(Mono.just(val)))
                            .doFinally(signalType -> clientSession.close());
                });
    }

    @Override
    public Mono<Void> deleteForm(String name, String clientId) {
        return Mono.from(client.startSession())
                .flatMap(clientSession -> {
                            clientSession.startTransaction();
                            ReactiveMongoOperations op = operations.withSession(clientSession);
                            return op
                                    .findAndRemove(Query.query(Criteria.where("_id").is(getMetaName(clientId, name)).and("clientId").is(clientId)),
                                            FormMeta.class,
                                            getMetaCollectionName())
                                    .onErrorMap(throwable -> ErrorEnum.DELETE_FORM_FAILED.details(throwable.getMessage()).getException())
                                    .switchIfEmpty(Mono.error(ErrorEnum.FORM_NOT_FOUND.getException()))
                                    .flatMap(formMeta -> op
                                            .remove(Query.query(Criteria.where("name").is(name).and("clientId").is(clientId)),
                                                    Form.class,
                                                    collectionName)
                                            .onErrorMap(throwable -> ErrorEnum.DELETE_FORM_FAILED.details(throwable.getMessage()).getException())
                                    )
                                    .onErrorResume(throwable -> Mono.from(clientSession.abortTransaction()).then(Mono.error(throwable)))
                                    .flatMap(val -> Mono.from(clientSession.commitTransaction()).then())
                                    .doFinally(signalType -> clientSession.close());
                        }
                );
    }

    @Override
    public Mono<QueryResult<Form>> listForm(String clientId) {
        return operations.find(Query.query(Criteria.where("clientId").is(clientId)), FormMeta.class, getMetaCollectionName())
                .collectList()
                .flatMapMany(formMetas -> {
                    Collection<String> ids = new HashSet<>();
                    for (FormMeta meta : formMetas) {
                        if (!StringUtils.hasText(meta.getCurrentId())) ;
                        ids.add(meta.getCurrentId());
                    }
                    return operations.find(Query.query(Criteria.where("_id").in(ids)), Form.class, collectionName);
                })
                .collectList()
                .map(forms -> new QueryResult<>(forms.size(), forms))
                .onErrorMap(throwable -> ErrorEnum.UNKNOWN.details(throwable.getMessage()).getException());
    }

    @Override
    public Mono<QueryResult<Form>> listForm(String clientId, String name) {
        return operations.find(Query.query(Criteria.where("clientId").is(clientId).and("name").is(name)), Form.class, collectionName)
                .collectList()
                .map(forms -> new QueryResult<>(forms.size(), forms))
                .onErrorMap(throwable -> ErrorEnum.UNKNOWN.details(throwable.getMessage()).getException());
    }

    @Override
    public Mono<Form> getForm(String name, String clientId) {
        return operations
                .findOne(Query.query(Criteria.where("name").is(getMetaName(clientId, name)).and("clientId").is(clientId)),
                        FormMeta.class,
                        getMetaCollectionName())
                .onErrorMap(throwable -> ErrorEnum.UNKNOWN.details(throwable.getMessage()).getException())
                .switchIfEmpty(Mono.error(ErrorEnum.FORM_NOT_FOUND.getException()))
                .flatMap(formMeta -> operations
                        .findOne(Query.query(Criteria.where("name").is(name).and("version").is(formMeta.version).and("clientId").is(clientId)),
                                Form.class,
                                collectionName)
                        .onErrorMap(throwable -> ErrorEnum.UNKNOWN.details(throwable.getMessage()).getException())
                        .switchIfEmpty(Mono.error(ErrorEnum.FORM_NOT_FOUND.getException())));
    }

    @Override
    public Mono<Form> getFormById(String id) {
        return operations.findById(id, Form.class, collectionName)
                .onErrorMap(throwable -> ErrorEnum.UNKNOWN.details(throwable.getMessage()).getException())
                .switchIfEmpty(Mono.error(ErrorEnum.FORM_NOT_FOUND.getException()));
    }

    protected String getMetaCollectionName() {
        return collectionName + "_meta";
    }

    protected String getMetaName(Form form) {
        return form.getClientId() + ":" + form.getName();
    }

    protected String getMetaName(String clientId, String formName) {
        return clientId + ":" + formName;
    }

    @Getter
    @Setter
    public static class FormMeta {

        @Id
        private String name;
        private String clientId;
        private String owner;
        private String currentId;
        private int version = 0;
    }

}
