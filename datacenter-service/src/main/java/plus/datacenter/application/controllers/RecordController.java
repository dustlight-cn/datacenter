package plus.datacenter.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import plus.auth.resources.AuthPrincipalUtil;
import plus.auth.resources.core.AuthPrincipal;
import plus.datacenter.core.DatacenterException;
import plus.datacenter.core.ErrorEnum;
import plus.datacenter.core.entities.forms.FormRecord;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;
import plus.datacenter.core.services.FormRecordService;
import plus.datacenter.core.services.FormService;
import plus.datacenter.core.utils.FormUtils;
import reactor.core.publisher.Mono;

import java.util.*;

@Tag(name = "Records", description = "表单记录")
@RestController
@RequestMapping("/v1/")
@SecurityRequirement(name = "auth")
@CrossOrigin
public class RecordController {

    @Autowired
    private FormService formService;

    @Autowired
    private FormRecordService formRecordService;

    @PostMapping("record")
    @Operation(summary = "创建表单记录", description = "提交一条表单记录。")
    public Mono<FormRecord> createRecord(@RequestBody FormRecord record,
                                         AbstractOAuth2TokenAuthenticationToken token) {
        AuthPrincipal principal = AuthPrincipalUtil.getAuthPrincipal(token);
        record.setFormId(null);
        record.setFormVersion(null);
        return validate(record, principal)
                .flatMap(record1 -> formRecordService.createRecord(record1))
                .onErrorMap(throwable -> throwable instanceof DatacenterException ? throwable : ErrorEnum.CREATE_RESOURCE_FAILED.details(throwable.getMessage()).getException());
    }

    @GetMapping("record/{id}")
    @Operation(summary = "获取表单记录", description = "获取一条表单记录。")
    public Mono<FormRecord> getRecord(@PathVariable String id,
                                      AbstractOAuth2TokenAuthenticationToken token) {
        AuthPrincipal principal = AuthPrincipalUtil.getAuthPrincipal(token);
        return formRecordService.getRecord(id);
    }

    @DeleteMapping("record/{id}")
    @Operation(summary = "删除表单记录", description = "删除一条表单记录。")
    public Mono<Void> deleteRecord(@PathVariable String id,
                                   AbstractOAuth2TokenAuthenticationToken token) {
        AuthPrincipal principal = AuthPrincipalUtil.getAuthPrincipal(token);
        return formRecordService.deleteRecord(id);
    }

    @PutMapping("record/{id}")
    @Operation(summary = "更新表单记录", description = "更新一条表单记录。")
    public Mono<FormRecord> updateRecord(@PathVariable String id,
                                         @RequestBody FormRecord record,
                                         AbstractOAuth2TokenAuthenticationToken token) {
        AuthPrincipal principal = AuthPrincipalUtil.getAuthPrincipal(token);
        return formRecordService.getRecord(id)
                .flatMap(record1 -> {
                    record.setId(record1.getId());
                    record.setFormId(record1.getFormId());
                    return validate(record, principal);
                })
                .flatMap(record1 -> {
                    record.setId(id);
                    record1.setCreatedAt(null);
                    record1.setOwner(null);
                    record1.setFormId(null);
                    record1.setFormVersion(null);
                    record1.setFormName(null);
                    return formRecordService.updateRecord(record1);
                })
                .onErrorMap(throwable -> throwable instanceof DatacenterException ? throwable : ErrorEnum.UPDATE_RESOURCE_FAILED.details(throwable.getMessage()).getException());
    }

    /**
     * 校验表单
     *
     * @param record
     * @param authPrincipal
     * @return
     */
    protected Mono<FormRecord> validate(FormRecord record, AuthPrincipal authPrincipal) {
        return (StringUtils.hasText(record.getFormId()) ?
                formService.getFormById(record.getFormId()) : formService.getForm(record.getFormName(), authPrincipal.getClientId()))
                .map(form -> {

                    record.setClientId(authPrincipal.getClientId());
                    record.setOwner(authPrincipal.getUidString());
                    record.setFormId(form.getId());
                    record.setFormName(form.getName());
                    record.setFormVersion(form.getVersion());


                    Map<String, Item> items = form.getItems();
                    Map<String, Object> data = record.getData();
                    Map<String, Object> validatedData = new HashMap<>();

                    Iterator<Map.Entry<String, Item>> iterator = items.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Item> kv = iterator.next();
                        String key = kv.getKey();
                        Item item = kv.getValue();
                        if (item == null)
                            continue;

                        if (item.getArray() != null && item.getArray()) {
                            // Item 为数组
                            Object value = data == null ? null : data.get(key);
                            if (!(value instanceof Collection))
                                throw new IllegalArgumentException("Validation failed: '" + key + "' require an array value");
                            Collection arrays = (Collection) value;
                            if ((arrays == null || arrays.size() == 0) && item.getRequired())
                                throw new IllegalArgumentException("Validation failed: '" + key + "' is empty but required");
                            ItemType t = item.getType();
                            int i = 0;
                            Collection<Object> transformedValues = new ArrayList<>();
                            for (Object v : arrays) {
                                v = FormUtils.transformItemValue(v, t);
                                if (!item.validate(v))
                                    throw new IllegalArgumentException("Validation failed: '" + key + "[" + i + "]'");
                                transformedValues.add(v);
                                i++;
                            }
                            validatedData.put(key, transformedValues);

                        } else {
                            // Item 不为数组
                            Object value = data == null ? null : data.get(key);
                            if (value instanceof Collection && item.getType() != ItemType.SELECT)
                                throw new IllegalArgumentException("Validation failed: '" + key + "' is not array");
                            value = FormUtils.transformItemValue(value, item.getType());
                            if (!item.validate(value))
                                throw new IllegalArgumentException("Validation failed: '" + key + "'");
                            validatedData.put(key, value);
                        }

                    }

                    record.setData(validatedData);

                    return record;
                });
    }
}
