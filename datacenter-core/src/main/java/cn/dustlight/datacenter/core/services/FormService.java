package cn.dustlight.datacenter.core.services;

import cn.dustlight.datacenter.core.entities.forms.Form;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * 表单服务，负责简单的表单增删改查。
 */
public interface FormService {

    Mono<Form> createForm(Form origin, String clientId);

    Flux<Form> createForms(Collection<Form> origins, String clientId);

    Mono<Form> getForm(String id, String clientId);

    Flux<Form> getForms(Collection<String> ids, String clientId);

    Mono<Form> getLatestForm(String name, String clientId);

    Flux<Form> getLatestForms(Collection<String> names, String clientId);

    Mono<Form> updateForm(Form target, String clientId);

    Flux<Form> updateForms(Collection<Form> targets, String clientId);

    Mono<Void> deleteForm(String name, String clientId);

    Mono<Void> deleteForms(Collection<String> names, String clientId);

}
