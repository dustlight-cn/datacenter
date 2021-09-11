package plus.datacenter.core.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.DatacenterPrincipal;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;
import plus.datacenter.core.entities.forms.Record;
import plus.datacenter.core.utils.FormUtils;
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

    private Map<ItemType, ItemValueTransformer> transformerMap = new HashMap<>();

    public DefaultRecordValidator(int order, FormService formService) {
        super(order);
        this.formService = formService;
    }

    public DefaultRecordValidator(FormService formService) {
        super();
        this.formService = formService;
    }

    @Override
    public Mono<Collection<Record>> validate(Collection<Record> records, DatacenterPrincipal principal, String clientId) {
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
                .flatMap(context -> doValidate(context))
                .collectList()
                .map(records1 -> records1);
    }

    protected Mono<Record> doValidate(Context context) {
        Form form = context.getForm();
        Record record = context.getRecord();
        DatacenterPrincipal principal = context.getPrincipal();
        String clientId = context.getClientId();

        record.setClientId(clientId);
        record.setOwner(principal.getUidAsString());
        record.setFormId(form.getId());
        record.setFormName(form.getName());
        record.setFormVersion(form.getVersion());

        Map<String, Item> itemMap = form.getItems();
        Map<String, Object> recordData = record.getData();
        Map<String, Object> validatedRecordData = new HashMap<>();

        Iterator<Map.Entry<String, Item>> iterator = itemMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Item> kv = iterator.next();

            String key = kv.getKey();
            Item item = kv.getValue(); // 被校验的 Item

            if (item == null)
                continue;

            boolean isItemRequired = item.getRequired() != null && item.getRequired(); // Item 是否必填
            ItemType itemType = item.getType(); // Item 类型

            Object itemValue = recordData == null ? null : recordData.get(key); // 被校验的值

            if (item.getArray() != null && item.getArray()) { // 当 Item 为数组时
                if (itemValue != null && !(itemValue instanceof Collection))
                    throw new IllegalArgumentException("Validation failed: '" + key + "' require an array value");
                Collection itemValueCollection = (Collection) itemValue;
                if (itemValueCollection == null) {
                    if (isItemRequired)
                        throw new IllegalArgumentException("Validation failed: '" + key + "' is null but required");
                } else if (itemValueCollection.size() == 0) {
                    if (isItemRequired)
                        throw new IllegalArgumentException("Validation failed: '" + key + "' is empty but required");
                } else {
                    int i = 0;
                    Collection<Object> transformedValues = new ArrayList<>();
                    for (Object originValue : itemValueCollection) {
                        Object transformedValue = FormUtils.transformItemValue(originValue, itemType);
                        if (!item.validate(transformedValue))
                            throw new IllegalArgumentException("Validation failed: '" + key + "[" + i + "]'");
                        transformedValues.add(transform(transformedValue, itemType));
                        i++;
                    }
                    validatedRecordData.put(key, transformedValues);
                }

            } else {
                if (itemValue instanceof Collection && itemType != ItemType.SELECT)
                    throw new IllegalArgumentException("Validation failed: '" + key + "' require an non-array value");
                if (itemValue != null && !(itemValue instanceof Collection) && itemType == ItemType.SELECT)
                    throw new IllegalArgumentException("Validation failed: '" + key + "' require an array value");
                Object transformedValue = FormUtils.transformItemValue(itemValue, itemType);
                if (!item.validate(transformedValue))
                    throw new IllegalArgumentException("Validation failed: '" + key + "'");
                validatedRecordData.put(key, transform(transformedValue, itemType));
            }
        }
        record.setData(validatedRecordData);
        return Mono.just(record);
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

    private Object transform(Object originObject, ItemType type) {
        ItemValueTransformer transformer = null;
        if (this.transformerMap == null || (transformer = this.transformerMap.get(type)) == null)
            return originObject;
        return transformer.transform(originObject);
    }

    public void setTransformers(ItemValueTransformer... transformers) {
        if (transformers == null)
            return;
        this.transformerMap.clear();
        for (ItemValueTransformer transformer : transformers)
            this.transformerMap.put(transformer.getItemType(), transformer);
    }

    public void setTransformers(Collection<ItemValueTransformer> transformers) {
        if (transformers == null)
            return;
        this.transformerMap.clear();
        for (ItemValueTransformer transformer : transformers)
            this.transformerMap.put(transformer.getItemType(), transformer);
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
