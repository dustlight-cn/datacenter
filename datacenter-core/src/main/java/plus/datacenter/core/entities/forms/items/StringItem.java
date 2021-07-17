package plus.datacenter.core.entities.forms.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;

import java.util.regex.Pattern;

@Getter
@Setter
public class StringItem extends Item<String> {

    private Boolean multiline;
    private Boolean html;
    private String regex;
    @JsonIgnore
    private Pattern pattern;

    @Override
    public Boolean validate(String value) {
        if (!super.validate(value))
            return false;
        if (regex == null)
            return true;
        return pattern.matcher(value).matches();
    }

    public void setRegex(String regex) {
        this.regex = regex;
        if (regex == null)
            pattern = null;
        else
            pattern = Pattern.compile(regex);
    }


    @Schema(defaultValue = "STRING")
    @Override
    public ItemType getType() {
        return ItemType.STRING;
    }

    @Override
    public void setType(ItemType type) {
        super.setType(ItemType.STRING);
    }
}
