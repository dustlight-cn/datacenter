package plus.datacenter.application.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plus.datacenter.elasticsearch.services.ElasticsearchFormSearcher;
import reactor.core.publisher.Mono;

@RequestMapping("/v1/test")
@RestController
@SecurityRequirement(name = "auth")
@CrossOrigin
public class TestController {


    @Autowired
    private ElasticsearchFormSearcher formSearcher;

    @GetMapping
    public Mono<?> search(String q) {
        return formSearcher.test(q);
    }
}
