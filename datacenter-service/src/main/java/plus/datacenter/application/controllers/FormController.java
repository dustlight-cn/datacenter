package plus.datacenter.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import plus.auth.client.reactive.ReactiveAuthClient;
import plus.auth.entities.QueryResult;
import plus.auth.resources.core.AuthPrincipal;
import plus.datacenter.application.ClientUtils;
import plus.datacenter.application.services.FormSchemaFiller;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.services.FormSearcher;
import plus.datacenter.core.services.FormService;
import reactor.core.publisher.Mono;

@Tag(name = "Forms", description = "表单")
@RestController
@RequestMapping("/v1/")
@SecurityRequirement(name = "auth")
@CrossOrigin("*")
public class FormController {

    @Autowired
    private FormService formService;

    @Autowired
    private FormSearcher formSearcher;

    @Autowired
    private FormSchemaFiller formSchemaFiller;

    @PostMapping("form")
    @Operation(summary = "创建表单", description = "创建一个表单，返回创建后的表单。")
    public Mono<Form> createForm(@RequestBody Form form,
                                 @RequestParam(name = "cid", required = false) String clientId,
                                 ReactiveAuthClient reactiveAuthClient,
                                 AuthPrincipal principal) {
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> {
                    form.setClientId(cid);
                    if (principal.getUid() != null)
                        form.setOwner(principal.getUidString());
                    return formService.createForm(form, cid);
                })
                .map(form1 -> formSchemaFiller.fill(form1));
    }

    @GetMapping("form")
    @Operation(summary = "获取最新的表单", description = "通过名称获取最新版本的表单。")
    public Mono<Form> getLatestForm(@RequestParam String name,
                                    @RequestParam(name = "cid", required = false) String clientId,
                                    ReactiveAuthClient reactiveAuthClient,
                                    AuthPrincipal principal) {
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> formService.getLatestForm(name, cid))
                .map(form1 -> formSchemaFiller.fill(form1));
    }

    @GetMapping("forms")
    @Operation(summary = "查询或列出表单", description = "当 query 不为空时，不分版本搜索表单，提供 name 可以限制搜索范围。" +
            "当 query 为空时，若 name 不为空则列出该名称表单的所有版本，否则列出此应用的所有最新表单结构。")
    public Mono<QueryResult<Form>> getForms(@RequestParam(required = false) String name,
                                            @RequestParam(required = false) String query,
                                            @RequestParam(required = false, defaultValue = "0") Integer page,
                                            @RequestParam(required = false, defaultValue = "10") Integer size,
                                            @RequestParam(name = "cid", required = false) String clientId,
                                            ReactiveAuthClient reactiveAuthClient,
                                            AuthPrincipal principal) {
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> StringUtils.hasText(name) ?
                        formSearcher.search(cid, query, name, page, size) :
                        formSearcher.search(cid, query, page, size)
                )
                .map(formQueryResult -> {
                    if(formQueryResult != null && formQueryResult.getData() != null)
                        formQueryResult.setData(formSchemaFiller.fill(formQueryResult.getData()));
                    return formQueryResult;
                });
    }

    @PutMapping("form")
    @Operation(summary = "更新表单", description = "通过名称更新表单结构。")
    public Mono<Form> updateForm(@RequestBody Form form,
                                 @RequestParam(name = "cid", required = false) String clientId,
                                 ReactiveAuthClient reactiveAuthClient,
                                 AuthPrincipal principal) {
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> {
                    if (principal.getUid() != null)
                        form.setOwner(principal.getUidString());
                    form.setOwner(principal.getUidString());
                    form.setClientId(cid);
                    return formService.updateForm(form, cid);
                })
                .map(form1 -> formSchemaFiller.fill(form1));
    }

    @DeleteMapping("forms")
    @Operation(summary = "删除表单", description = "通过名称删除所有表单。")
    public Mono<Void> deleteForm(@RequestParam(name = "name") String name,
                                 @RequestParam(name = "cid", required = false) String clientId,
                                 ReactiveAuthClient reactiveAuthClient,
                                 AuthPrincipal principal) {
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> formService.deleteForm(name, cid));
    }

    @GetMapping("form/{id}")
    @Operation(summary = "获取表单", description = "通过 ID 获取表单结构。")
    public Mono<Form> getFormById(@PathVariable String id,
                                  @RequestParam(name = "cid", required = false) String clientId,
                                  ReactiveAuthClient reactiveAuthClient,
                                  AuthPrincipal principal) {
        return ClientUtils.obtainClientId(reactiveAuthClient, clientId, principal)
                .flatMap(cid -> formService.getForm(id, cid))
                .map(form1 -> formSchemaFiller.fill(form1));
    }

}
