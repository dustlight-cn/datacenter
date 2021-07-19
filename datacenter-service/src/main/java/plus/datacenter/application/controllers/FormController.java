package plus.datacenter.application.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import plus.auth.entities.QueryResult;
import plus.auth.resources.AuthPrincipalUtil;
import plus.auth.resources.core.AuthPrincipal;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.services.FormSearcher;
import plus.datacenter.core.services.FormService;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Tag(name = "Forms", description = "表单")
@RestController
@RequestMapping("/v1/forms")
@SecurityRequirement(name = "auth")
@CrossOrigin
public class FormController {

    @Autowired
    private FormService formService;

    @Autowired
    private FormSearcher formSearcher;

    @PostMapping
    public Mono<Form> createForm(@RequestBody Form form,
                                 AbstractOAuth2TokenAuthenticationToken token) {
        AuthPrincipal principal = AuthPrincipalUtil.getAuthPrincipal(token);
        form.setClientId(principal.getClientId());
        form.setOwner(principal.getUidString());
        return formService.createForm(form);
    }

    @GetMapping
    public Mono<QueryResult<Form>> getForms(@RequestParam(required = false) String name,
                                            @RequestParam(required = false) String query,
                                            @RequestParam(required = false, defaultValue = "0") Integer page,
                                            @RequestParam(required = false, defaultValue = "10") Integer size,
                                            AbstractOAuth2TokenAuthenticationToken token) {
        AuthPrincipal principal = AuthPrincipalUtil.getAuthPrincipal(token);
        if (StringUtils.hasText(name))
            return formService.getForm(name, principal.getClientId()).map(form -> new QueryResult(1, Arrays.asList(form)));
        else if (StringUtils.hasText(query))
            return formSearcher.search(principal.getClientId(), query, page, size);
        return formService.listForm(principal.getClientId());
    }

    @PutMapping
    public Mono<Form> updateForm(@RequestBody Form form,
                                 AbstractOAuth2TokenAuthenticationToken token) {
        AuthPrincipal principal = AuthPrincipalUtil.getAuthPrincipal(token);
        form.setOwner(principal.getUidString());
        form.setClientId(principal.getClientId());
        return formService.updateForm(form);
    }

    @DeleteMapping("")
    public Mono<Void> deleteForm(@RequestParam(name = "name") String name,
                                 AbstractOAuth2TokenAuthenticationToken token) {
        AuthPrincipal principal = AuthPrincipalUtil.getAuthPrincipal(token);
        return formService.deleteForm(name, principal.getClientId());
    }

    @GetMapping("/{id}")
    public Mono<Form> getFormById(@PathVariable String id) {
        return formService.getFormById(id);
    }

}
