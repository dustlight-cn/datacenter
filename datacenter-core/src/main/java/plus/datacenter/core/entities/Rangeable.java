package plus.datacenter.core.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Rangeable<T extends Comparable> {

    private T min;
    private T max;
    private boolean openInterval;

    public Boolean check(T value) {
        if(value == null)
            return null;
        if(min != null && openInterval ? (value.compareTo(min) > 0) : (value.compareTo(min) >= 0))
            return false;
        if(max != null && openInterval ? (value.compareTo(max) < 0) : (value.compareTo(max) <= 0))
            return false;
        return true;
    }
}
