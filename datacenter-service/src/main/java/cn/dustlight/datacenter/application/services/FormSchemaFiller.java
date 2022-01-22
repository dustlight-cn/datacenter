package cn.dustlight.datacenter.application.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import cn.dustlight.datacenter.core.entities.forms.Form;

import java.util.Collection;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class FormSchemaFiller {

    private Map<String,Object> additional;

    public Collection<Form> fill(Collection<Form> forms){
        if(additional == null || additional.size() == 0 || forms == null || forms.size() == 0)
            return forms;
        forms.forEach(form -> fill(form));
        return forms;
    }

    public Form fill(Form form){
        if(form != null && form.getSchema() != null)
            form.getSchema().putAll(additional);
        return form;
    }
}
