package plus.datacenter.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
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

import java.time.Instant;
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
    public Mono<FormRecord> createRecord(@RequestParam String formName,
                                         @RequestBody FormRecord record,
                                         AbstractOAuth2TokenAuthenticationToken token) {
        AuthPrincipal principal = AuthPrincipalUtil.getAuthPrincipal(token);
        return formService.getForm(formName, principal.getClientId())
                .flatMap(form -> {

                    record.setClientId(principal.getClientId());
                    record.setOwner(principal.getUidString());
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
                                return Mono.error(ErrorEnum.CREATE_RESOURCE_FAILED.details("Validation failed: '" + key + "' require an array value").getException());
                            Collection arrays = (Collection) value;
                            if ((arrays == null || arrays.size() == 0) && item.getRequired())
                                return Mono.error(ErrorEnum.CREATE_RESOURCE_FAILED.details("Validation failed: '" + key + "' is empty but required").getException());
                            ItemType t = item.getType();
                            int i = 0;
                            Collection<Object> transformedValues = new ArrayList<>();
                            for (Object v : arrays) {
                                v = FormUtils.transformItemValue(v, t);
                                if (!item.validate(v))
                                    return Mono.error(ErrorEnum.CREATE_RESOURCE_FAILED.details("Validation failed: '" + key + "[" + i + "]'").getException());
                                transformedValues.add(v);
                                i++;
                            }
                            validatedData.put(key, transformedValues);

                        } else {
                            // Item 不为数组
                            Object value = data == null ? null : data.get(key);
                            if (value instanceof Collection && item.getType() != ItemType.SELECT)
                                return Mono.error(ErrorEnum.CREATE_RESOURCE_FAILED.details("Validation failed: '" + key + "' is not array").getException());
                            value = FormUtils.transformItemValue(value, item.getType());
                            if (!item.validate(value))
                                return Mono.error(ErrorEnum.CREATE_RESOURCE_FAILED.details("Validation failed: '" + key + "'").getException());
                            validatedData.put(key, value);
                        }

                    }

                    record.setFormId(form.getId());
                    record.setData(validatedData);

                    return formRecordService.createRecord(record);
                })
                .onErrorMap(throwable -> {
                    throwable.printStackTrace();
                    return throwable instanceof DatacenterException ? throwable : ErrorEnum.CREATE_RESOURCE_FAILED.details(throwable.getMessage()).getException();
                });
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
    public Mono<FormRecord> deleteRecord(@PathVariable String id,
                                         @RequestBody FormRecord record,
                                         AbstractOAuth2TokenAuthenticationToken token) {
        AuthPrincipal principal = AuthPrincipalUtil.getAuthPrincipal(token);
        record.setId(id);
        record.setCreatedAt(null);
        record.setOwner(null);
        record.setFormId(null);
        record.setFormVersion(null);
        record.setFormName(null);
        record.setClientId(principal.getClientId());
        record.setUpdatedAt(Instant.now());
        return formRecordService.updateRecord(record);
    }
}
