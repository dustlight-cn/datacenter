package plus.datacenter.mongo.services;

import com.mongodb.reactivestreams.client.MongoClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.services.FormService;
import reactor.core.publisher.Mono;

import java.util.Date;

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
        return Mono.from(client.startSession())
                .flatMap(clientSession -> {
                    clientSession.startTransaction();
                    return Mono.just(clientSession);
                })
                .flatMap(clientSession -> operations.withSession(clientSession)
                        .insert(meta, getMetaCollectionName())
                        .onErrorResume(throwable -> Mono.from(clientSession.abortTransaction()).then(Mono.error((throwable instanceof DuplicateKeyException) ?
                                ErrorEnum.FORM_EXISTS.details(throwable.getMessage()).getException() :
                                ErrorEnum.CREATE_FORM_FAILED.details(throwable.getMessage()).getException())))
                        .flatMap(formMeta -> {
                            origin.setId(null);
                            Date t = new Date();
                            origin.setVersion(formMeta.version);
                            origin.setCreatedAt(t);
                            return operations.withSession(clientSession)
                                    .insert(origin, collectionName);
                        })
                        .onErrorResume(throwable -> Mono.from(clientSession.abortTransaction()).then(Mono.error(ErrorEnum.CREATE_FORM_FAILED.details(throwable.getMessage()).getException())))
                        .flatMap(val -> Mono.from(clientSession.commitTransaction()).then(Mono.just(val)))
                        .doFinally(signalType -> clientSession.close())
                );
    }

    @Override
    public Mono<Form> updateForm(Form target) {
        Update update = new Update();
        update.inc("version", 1);
        if (target.getOwner() != null)
            update.set("owner", target.getOwner());
        return Mono.from(client.startSession())
                .flatMap(clientSession -> {
                    clientSession.startTransaction();
                    return Mono.just(clientSession);
                })
                .flatMap(clientSession -> operations.withSession(clientSession)
                        .findAndModify(Query.query(Criteria.where("_id").is(getMetaName(target)).and("clientId").is(target.getClientId())),
                                update,
                                FormMeta.class,
                                getMetaCollectionName())
                        .onErrorResume(throwable -> Mono.from(clientSession.abortTransaction()).then(Mono.error(ErrorEnum.UPDATE_FORM_FAILED.details(throwable.getMessage()).getException())))
                        .switchIfEmpty(Mono.from(clientSession.abortTransaction()).then(Mono.error(ErrorEnum.FORM_NOT_FOUND.getException())))
                        .flatMap(formMeta -> {
                            target.setId(null);
                            target.setCreatedAt(new Date());
                            target.setVersion(formMeta.version + 1);
                            return operations.withSession(clientSession)
                                    .insert(target, collectionName);
                        })
                        .onErrorResume(throwable -> Mono.from(clientSession.abortTransaction()).then(Mono.error(ErrorEnum.UPDATE_FORM_FAILED.details(throwable.getMessage()).getException())))
                        .flatMap(val -> Mono.from(clientSession.commitTransaction()).then(Mono.just(val)))
                        .doFinally(signalType -> clientSession.close())
                );
    }

    @Override
    public Mono<Void> deleteForm(String name, String clientId) {
        return Mono.from(client.startSession())
                .flatMap(clientSession -> {
                    clientSession.startTransaction();
                    return Mono.just(clientSession);
                })
                .flatMap(clientSession -> operations.withSession(clientSession)
                        .findAndRemove(Query.query(Criteria.where("_id").is(getMetaName(clientId, name)).and("clientId").is(clientId)),
                                FormMeta.class,
                                getMetaCollectionName())
                        .onErrorResume(throwable -> Mono.from(clientSession.abortTransaction()).then(Mono.error(ErrorEnum.DELETE_FORM_FAILED.details(throwable.getMessage()).getException())))
                        .switchIfEmpty(Mono.from(clientSession.abortTransaction()).then(Mono.error(ErrorEnum.FORM_NOT_FOUND.getException())))
                        .flatMap(formMeta -> operations.withSession(clientSession).
                                remove(Query.query(Criteria.where("name").is(name).and("clientId").is(clientId)),
                                        Form.class,
                                        collectionName))
                        .onErrorResume(throwable -> Mono.from(clientSession.abortTransaction()).then(Mono.error(ErrorEnum.DELETE_FORM_FAILED.details(throwable.getMessage()).getException())))
                        .flatMap(val -> Mono.from(clientSession.commitTransaction()).then())
                        .doFinally(signalType -> clientSession.close()));
    }

    @Override
    public Mono<Form> getForm(String name, String clientId) {
        return operations
                .findOne(Query.query(Criteria.where("name").is(getMetaName(clientId, name)).and("clientId").is(clientId)),
                        FormMeta.class,
                        getMetaCollectionName())
                .onErrorMap(throwable -> ErrorEnum.FORM_NOT_FOUND.details(throwable.getMessage()).getException())
                .switchIfEmpty(Mono.error(ErrorEnum.FORM_NOT_FOUND.getException()))
                .flatMap(formMeta -> operations
                        .findOne(Query.query(Criteria.where("name").is(name).and("version").is(formMeta.version).and("clientId").is(clientId)),
                                Form.class,
                                collectionName)
                        .onErrorMap(throwable -> ErrorEnum.FORM_NOT_FOUND.details(throwable.getMessage()).getException())
                        .switchIfEmpty(Mono.error(ErrorEnum.FORM_NOT_FOUND.getException())));
    }

    @Override
    public Mono<Form> getFormById(String id) {
        return operations.findById(id, Form.class, collectionName)
                .onErrorMap(throwable -> ErrorEnum.FORM_NOT_FOUND.details(throwable.getMessage()).getException())
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
        private int version = 0;

    }

}
