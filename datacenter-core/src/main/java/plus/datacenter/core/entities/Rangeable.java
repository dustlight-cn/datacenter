package plus.datacenter.core.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Rangeable<T extends Comparable> implements Validatable<T> {

    private T min;
    private T max;
    private boolean openInterval;

    @Override
    public Boolean validate(T value) {
        if (value == null)
            return min == null && max == null;
        if (min != null && openInterval ? (value.compareTo(min) > 0) : (value.compareTo(min) >= 0))
            return false;
        if (max != null && openInterval ? (value.compareTo(max) < 0) : (value.compareTo(max) <= 0))
            return false;
        return true;
    }
}
