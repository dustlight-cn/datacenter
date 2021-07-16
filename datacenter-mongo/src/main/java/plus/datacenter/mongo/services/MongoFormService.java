package plus.datacenter.mongo.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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

    private ReactiveMongoOperations operations;
    private String collectionName;

    @Override
    public Mono<Form> createForm(Form origin) {
        if (!StringUtils.hasText(origin.getName()))
            ErrorEnum.CREATE_FORM_FAILED.details("form name can't not be null!").throwException();
        FormMeta meta = new FormMeta();
        meta.setName(origin.getName());
        meta.setVersion(0);
        return operations.insert(meta, getMetaCollectionName())
                .onErrorMap(throwable -> {
                    if (throwable instanceof DuplicateKeyException)
                        return ErrorEnum.FORM_EXISTS.details(throwable.getMessage()).getException();
                    return ErrorEnum.CREATE_FORM_FAILED.details(throwable.getMessage()).getException();
                })
                .flatMap(formMeta -> {
                    origin.setId(null);
                    Date t = new Date();
                    origin.setVersion(formMeta.version);
                    origin.setCreatedAt(t);
                    return operations.insert(origin, collectionName)
                            .onErrorMap(throwable -> ErrorEnum.CREATE_FORM_FAILED.details(throwable.getMessage()).getException());
                });
    }

    @Override
    public Mono<Form> updateForm(Form target) {
        Update update = new Update();
        update.inc("version", 1);
        return operations.findAndModify(Query.query(Criteria.where("name").is(target.getName())),
                update,
                FormMeta.class,
                getMetaCollectionName())
                .onErrorMap(throwable -> ErrorEnum.UPDATE_FORM_FAILED.details(throwable.getMessage()).getException())
                .switchIfEmpty(Mono.error(ErrorEnum.FORM_NOT_FOUND.getException()))
                .flatMap(formMeta -> {
                    target.setId(null);
                    target.setCreatedAt(new Date());
                    target.setVersion(formMeta.version + 1);
                    return operations.insert(target, collectionName)
                            .onErrorMap(throwable -> ErrorEnum.UPDATE_FORM_FAILED.details(throwable.getMessage()).getException());
                });
    }

    @Override
    public Mono<Void> deleteForm(String name) {
        return operations.findAndRemove(Query.query(Criteria.where("_id").is(name)), Form.class, getMetaCollectionName())
                .onErrorMap(throwable -> ErrorEnum.DELETE_FORM_FAILED.details(throwable.getMessage()).getException())
                .switchIfEmpty(Mono.error(ErrorEnum.FORM_NOT_FOUND.getException()))
                .flatMap(formMeta -> operations.remove(Query.query(Criteria.where("name").is(name)), Form.class, collectionName)
                        .onErrorMap(throwable -> ErrorEnum.DELETE_FORM_FAILED.details(throwable.getMessage()).getException()))
                .then();
    }

    @Override
    public Mono<Form> getForm(String name) {
        return operations.findOne(Query.query(Criteria.where("name").is(name)),
                FormMeta.class,
                getMetaCollectionName())
                .onErrorMap(throwable -> ErrorEnum.FORM_NOT_FOUND.details(throwable.getMessage()).getException())
                .switchIfEmpty(Mono.error(ErrorEnum.FORM_NOT_FOUND.getException()))
                .flatMap(formMeta -> operations.findOne(Query.query(Criteria.where("name").is(name)
                        .and("version").is(formMeta.version)), Form.class, collectionName)
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

    @Getter
    @Setter
    public static class FormMeta {

        @Id
        private String name;
        private int version = 0;

    }

}
