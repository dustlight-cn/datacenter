package cn.dustlight.datacenter.es2mongo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
public class FormMeta {

    @Id
    private String name;
    private String clientId;
    private String owner;
    private String currentId;
    private int version = 0;
}
