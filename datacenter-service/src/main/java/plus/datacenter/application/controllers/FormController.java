package plus.datacenter.application.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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
    public Mono<Form> createForm(@RequestBody Form form) {
        return formService.createForm(form);
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
