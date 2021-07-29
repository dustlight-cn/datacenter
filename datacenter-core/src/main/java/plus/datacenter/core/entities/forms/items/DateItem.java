package plus.datacenter.core.entities.forms.items;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import plus.datacenter.core.entities.Validatable;
import plus.datacenter.core.entities.forms.Item;
import plus.datacenter.core.entities.forms.ItemType;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
public class DateItem extends Item<Instant> {

    private InstantRangeable dateRange;

    @Override
    public Boolean validate(Instant value) {
        if (value == null)
            return super.validate(null);
        return super.validate(value) && (dateRange == null || dateRange.validate(value));
    }

    @Schema(defaultValue = "DATE")
    @Override
    public ItemType getType() {
        return ItemType.DATE;
    }

    @Override
    public void setType(ItemType type) {
        super.setType(ItemType.DATE);
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class InstantRangeable implements Validatable<Instant>, Serializable {

        @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
        private Instant min;

        @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
        private Instant max;

        private boolean openInterval;

        @Override
        public Boolean validate(Instant value) {
            if (value == null)
                return min == null && max == null;
            if (min != null && (openInterval ? (value.compareTo(min) < 0) : (value.compareTo(min) <= 0)))
                return false;
            if (max != null && (openInterval ? (value.compareTo(max) > 0) : (value.compareTo(max) >= 0)))
                return false;
            return true;
        }
    }

}
