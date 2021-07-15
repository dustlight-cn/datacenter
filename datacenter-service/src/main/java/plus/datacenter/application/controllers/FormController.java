package plus.datacenter.application.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "Forms", description = "表单")
@RestController
@RequestMapping("/v1/forms")
@SecurityRequirement(name = "auth")
@CrossOrigin
public class FormController {

    @GetMapping("/{id}")
    public Mono<String> test(@PathVariable String id) {
        return Mono.just(id);
    }
}
