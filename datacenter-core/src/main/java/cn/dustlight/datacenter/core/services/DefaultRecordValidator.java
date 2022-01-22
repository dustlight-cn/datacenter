package cn.dustlight.datacenter.core.services;

import cn.dustlight.datacenter.core.ErrorEnum;
import cn.dustlight.datacenter.core.utils.FormUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;
import cn.dustlight.datacenter.core.entities.DatacenterPrincipal;
import cn.dustlight.datacenter.core.entities.forms.Form;
import cn.dustlight.datacenter.core.entities.forms.Record;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;

/**
 * 默认记录验证器：通过 FormService 获取表单结构，对记录值分别进行验证。
 */
public class DefaultRecordValidator extends AbstractRecordValidator {

    @Getter
    @Setter
    private FormService formService;

    private Collection<ItemValueTransformer> transformers = new HashSet<>();

    public DefaultRecordValidator(int order, FormService formService) {
        super(order);
        this.formService = formService;
    }

    public DefaultRecordValidator(FormService formService) {
        super();
        this.formService = formService;
    }

    @Override
    public Mono<Collection<Record>> validate(Collection<Record> records,
                                             DatacenterPrincipal principal,
                                             String clientId) {
        if (records == null || records.size() == 0)
            return Mono.empty();
        return getForms(records, clientId)
                .collectList()
                .flatMapMany(forms -> {
                    Map<String, Form> formIdMap = buildFormMap(forms, form -> form.getId()); // 表单 ID 映射
                    Map<String, Form> formNameMap = buildFormMap(forms, form -> form.getName()); // 表单名称映射

                    Collection<Context> contexts = new HashSet<>();
                    for (Record record : records) {
                        Form form = StringUtils.hasText(record.getFormId()) ?
                                formIdMap.get(record.getFormId().trim()) :
                                formNameMap.get(record.getFormName().trim());
                        if (form == null)
                            return Mono.error(ErrorEnum.FORM_NOT_FOUND.getException());
                        contexts.add(new Context(form, record, principal, clientId));
                    }

                    return Flux.fromIterable(contexts);
                })
                .flatMap(context -> beforeValidate(context))
                .flatMap(context -> doValidate(context))
                .flatMap(context -> afterValidate(context))
                .collectList()
                .map(records1 -> records1);
    }

    protected Mono<Context> beforeValidate(Context context) {
        Form form = context.getForm();
        Record record = context.getRecord();
        DatacenterPrincipal principal = context.getPrincipal();
        String clientId = context.getClientId();

        record.setClientId(clientId);
        record.setOwner(principal.getUidAsString());
        record.setFormId(form.getId());
        record.setFormName(form.getName());
        record.setFormVersion(form.getVersion());

        return Mono.just(context);
    }

    protected Mono<Context> doValidate(Context context) {
        return Mono.just(context);
    }

    protected Mono<Record> afterValidate(Context context) {
        Form form = context.getForm();
        Record record = context.getRecord();
        Map<String, Object> data = record.getData();

        JsonNode schema = FormUtils.transformMapToJsonNode(form.getSchema()).get(FormUtils.propertiesFieldName);
        Iterator<Map.Entry<String, JsonNode>> iter = schema.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> kv = iter.next();
            String name = kv.getKey();
            JsonNode itemSchema = kv.getValue();
            if (!data.containsKey(name))
                continue;
            Object val = data.get(name);
            val = transform(val, itemSchema);
            data.put(name, val);
        }
        return Mono.just(context.getRecord());
    }

    /**
     * 根据记录集合获取相关表单
     *
     * @param records
     * @param clientId
     * @return
     */
    private Flux<Form> getForms(Collection<Record> records, String clientId) {
        Collection<String> formIds = new HashSet<>();
        Collection<String> formNames = new HashSet<>();
        for (Record record : records) {
            if (StringUtils.hasText(record.getFormId()))
                formIds.add(record.getFormId().trim());
            else if (StringUtils.hasText(record.getFormName()))
                formNames.add(record.getFormName().trim());
            else
                return Flux.error(ErrorEnum.FORM_NOT_FOUND.getException());
        }
        Flux<Form> formFlux = null;
        if (formIds.size() > 0)
            formFlux = formService.getForms(formIds, clientId)
                    .map(form -> {
                        formNames.remove(form.getName().trim());
                        return form;
                    });
        Flux<Form> tmp = null;
        if (formNames.size() > 0)
            tmp = formService.getLatestForms(formNames, clientId);
        if (tmp != null) {
            if (formFlux == null)
                formFlux = tmp;
            else
                formFlux.concatWith(tmp);
        }
        return tmp == null ? Flux.empty() : formFlux;
    }

    /**
     * 创建表单字典
     *
     * @param forms      表单集合
     * @param keyMapping Key 映射
     * @return
     */
    private Map<String, Form> buildFormMap(Collection<Form> forms, Function<Form, String> keyMapping) {
        if (forms == null || forms.size() == 0)
            return Collections.emptyMap();
        Map<String, Form> map = new HashMap<>();
        for (Form form : forms) {
            map.put(keyMapping.apply(form), form);
        }
        return map;
    }

    private Object transform(Object originObject, JsonNode schema) {
        if (this.transformers == null || this.transformers.size() == 0)
            return originObject;
        for (ItemValueTransformer transformer : transformers) {
            if (transformer.check(schema))
                return transformer.transform(originObject);
        }
        return originObject;
    }

    public void setTransformers(ItemValueTransformer... transformers) {
        if (transformers == null)
            return;
        this.transformers.clear();
        this.transformers.addAll(Arrays.asList(transformers));
    }

    public void setTransformers(Collection<ItemValueTransformer> transformers) {
        if (transformers == null)
            return;
        this.transformers.clear();
        this.transformers.addAll(transformers);
    }

    @Getter
    @AllArgsConstructor
    public static class Context {

        private Form form;
        private Record record;
        private DatacenterPrincipal principal;
        private String clientId;

    }
}
