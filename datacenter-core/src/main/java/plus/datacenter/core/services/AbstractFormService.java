package plus.datacenter.core.services;

import org.springframework.util.StringUtils;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.Form;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;

/**
 * 抽象表单服务
 */
public abstract class AbstractFormService implements FormService {

    @Override
    public Mono<Form> createForm(Form origin, String clientId) {
        beforeCreate(origin, clientId, Instant.now());
        return doInsert(Arrays.asList(origin))
                .singleOrEmpty()
                .switchIfEmpty(Mono.error(ErrorEnum.CREATE_FORM_FAILED.getException()));
    }

    @Override
    public Flux<Form> createForms(Collection<Form> origins, String clientId) {
        Instant now = Instant.now();

        for (Form form : origins) {
            beforeCreate(form, clientId, now);
        }
        return doInsert(origins);
    }

    @Override
    public Mono<Form> getForm(String id, String clientId) {
        return doGet(Arrays.asList(id), clientId)
                .singleOrEmpty()
                .switchIfEmpty(Mono.error(ErrorEnum.FORM_NOT_FOUND.getException()));
    }

    @Override
    public Flux<Form> getForms(Collection<String> ids, String clientId) {
        return doGet(ids, clientId);
    }

    @Override
    public Mono<Form> getLatestForm(String name, String clientId) {
        return doGetLatest(Arrays.asList(name), clientId)
                .singleOrEmpty()
                .switchIfEmpty(Mono.error(ErrorEnum.FORM_NOT_FOUND.getException()));
    }

    @Override
    public Flux<Form> getLatestForms(Collection<String> names, String clientId) {
        return doGetLatest(names, clientId);
    }

    @Override
    public Mono<Form> updateForm(Form target, String clientId) {
        beforeUpdate(target, clientId, Instant.now());
        return doUpdate(Arrays.asList(target))
                .singleOrEmpty()
                .switchIfEmpty(Mono.error(ErrorEnum.UPDATE_FORM_FAILED.getException()));
    }

    @Override
    public Flux<Form> updateForms(Collection<Form> targets, String clientId) {
        beforeUpdate(targets, clientId, Instant.now());
        return doUpdate(targets);
    }

    @Override
    public Mono<Void> deleteForm(String name, String clientId) {
        return doDelete(Arrays.asList(name), clientId);
    }

    @Override
    public Mono<Void> deleteForms(Collection<String> names, String clientId) {
        return doDelete(names, clientId);
    }

    protected abstract Flux<Form> doInsert(Collection<Form> origins);

    protected abstract Flux<Form> doGet(Collection<String> ids, String clientId);

    protected abstract Flux<Form> doGetLatest(Collection<String> names, String clientId);

    protected abstract Flux<Form> doUpdate(Collection<Form> template);

    protected abstract Mono<Void> doDelete(Collection<String> names, String clientId);

    protected void beforeCreate(Form form, String clientId, Instant now) {
        if (form == null) // 创建对象不可为空
            throw ErrorEnum.CREATE_FORM_FAILED.details("Form can not be null").getException();
        if (!StringUtils.hasText(form.getName())) // 表单名称不可为空
            throw ErrorEnum.CREATE_RECORD_FAILED.details("Form name must be set").getException();
        if (!StringUtils.hasText(clientId)) // Client ID 不可为空
            throw ErrorEnum.CREATE_RECORD_FAILED.details("Client ID must be set").getException();

        // 设置创建时间
        form.setCreatedAt(now);

        // 设置 Client ID
        form.setClientId(clientId);
    }

    protected void beforeUpdate(Form target, String clientId, Instant now) {
        beforeCreate(target, clientId, now);
    }

    protected void beforeUpdate(Collection<Form> targets, String clientId, Instant now) {
        for (Form target : targets) {
            beforeUpdate(target, clientId, now);
        }
    }
}
