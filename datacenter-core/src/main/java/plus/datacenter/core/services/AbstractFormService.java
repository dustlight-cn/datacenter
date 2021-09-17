package plus.datacenter.core.services;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.utils.FormUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;

/**
 * 抽象表单服务
 */
public abstract class AbstractFormService implements FormService {

    @Getter
    @Setter
    private PrincipalHolder principalHolder;

    @Getter
    private List<FormValidator> validators = new ArrayList<>();

    @Override
    public Mono<Form> createForm(Form origin, String clientId) {
        return createForms(Arrays.asList(origin), clientId)
                .singleOrEmpty()
                .switchIfEmpty(Mono.error(ErrorEnum.CREATE_FORM_FAILED.getException()));
    }

    @Override
    public Flux<Form> createForms(Collection<Form> origins, String clientId) {
        Instant now = Instant.now();

        for (Form form : origins) {
            beforeCreate(form, clientId, now);
        }
        return joinValidator(origins, clientId)
                .flatMapMany(forms -> doInsert(origins));
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
        return updateForms(Arrays.asList(target), clientId)
                .singleOrEmpty()
                .switchIfEmpty(Mono.error(ErrorEnum.UPDATE_FORM_FAILED.getException()));
    }

    @Override
    public Flux<Form> updateForms(Collection<Form> targets, String clientId) {
        beforeUpdate(targets, clientId, Instant.now());
        return joinValidator(targets, clientId)
                .flatMapMany(forms -> doUpdate(targets));
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
        form.setId(null);
        FormUtils.fillReference(form);
    }

    protected void beforeUpdate(Form target, String clientId, Instant now) {
        beforeCreate(target, clientId, now);
    }

    protected void beforeUpdate(Collection<Form> targets, String clientId, Instant now) {
        for (Form target : targets) {
            beforeUpdate(target, clientId, now);
        }
    }

    /**
     * 执行校验
     *
     * @param forms
     * @param clientId
     * @return
     */
    private Mono<Collection<Form>> joinValidator(Collection<Form> forms, String clientId) {
        if (validators == null || validators.size() == 0)
            return Mono.just(forms);
        if (principalHolder == null)
            return Mono.error(new DatacenterException("PrincipalHolder must be set"));
        return principalHolder.getPrincipal()
                .switchIfEmpty(Mono.error(new DatacenterException("principal is null")))
                .flatMap(principal -> {
                    Mono<Collection<Form>> result = Mono.just(forms);
                    for (FormValidator validator : validators) {
                        result = result.flatMap(formz -> validator.validate(formz, principal, clientId));
                    }
                    return result;
                });
    }

    public void addValidator(FormValidator... validator) {
        this.validators.addAll(Arrays.asList(validator));
        this.validators.sort(Comparator.comparingInt(Ordered::getOrder));
    }

    public void removeValidator(FormValidator... validator) {
        this.validators.remove(Arrays.asList(validator));
    }

    public void addValidator(Collection<FormValidator> validators) {
        this.validators.addAll(validators);
        this.validators.sort(Comparator.comparingInt(Ordered::getOrder));
    }

    public void removeValidator(Collection<FormValidator> validators) {
        this.validators.remove(Arrays.asList(validators));
    }
}
