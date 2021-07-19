package plus.datacenter.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import plus.auth.resources.AuthPrincipalUtil;
import plus.auth.resources.core.AuthPrincipal;
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
@RequestMapping("/v1/records")
@SecurityRequirement(name = "auth")
@CrossOrigin
public class RecordController {

    @Autowired
    private FormService formService;

    @Autowired
    private FormRecordService formRecordService;

    @PostMapping
    @Operation(summary = "创建表单记录", description = "提交表单记录。")
    public Mono<FormRecord> createRecord(@RequestBody FormRecord record,
                                         AbstractOAuth2TokenAuthenticationToken token) {
        return formService.getFormById(record.getFormId())
                .flatMap(form -> {

                    AuthPrincipal principal = AuthPrincipalUtil.getAuthPrincipal(token);
                    record.setClientId(principal.getClientId());
                    record.setOwner(principal.getUidString());

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

                        if (item.getArray()) {
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
                            if (value instanceof Collection)
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
                .onErrorMap(throwable -> ErrorEnum.CREATE_RESOURCE_FAILED.details(throwable.getMessage()).getException());
    }
}
