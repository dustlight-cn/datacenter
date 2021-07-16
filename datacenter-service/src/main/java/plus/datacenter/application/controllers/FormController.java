package plus.datacenter.application.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import plus.auth.resources.AuthPrincipalUtil;
import plus.auth.resources.core.AuthPrincipal;
import plus.datacenter.core.entities.forms.Form;
import plus.datacenter.core.services.FormService;
import reactor.core.publisher.Mono;

@Tag(name = "Forms", description = "表单")
@RestController
@RequestMapping("/v1/forms")
@SecurityRequirement(name = "auth")
@CrossOrigin
public class FormController {

    @Autowired
    private FormService formService;

    @PostMapping
    public Mono<Form> createForm(@RequestBody Form form,
                                 AbstractOAuth2TokenAuthenticationToken token) {
        AuthPrincipal principal = AuthPrincipalUtil.getAuthPrincipal(token);
        form.setClientId(principal.getClientId());
        form.setOwner(principal.getUid().toString());
        return formService.createForm(form);
    }

    @GetMapping("/{id}")
    public Mono<Form> getForm(@PathVariable String id) {
        return formService.getForm(id);
    }

    @PutMapping
    public Mono<Form> updateForm(@RequestBody Form form) {
        return formService.updateForm(form);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteForm(@PathVariable String id) {
        return formService.deleteForm(id);
    }
}
