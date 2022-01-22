package cn.dustlight.datacenter.core.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rangeable<T extends Comparable> implements Validatable<T>, Serializable {

    private T min;
    private T max;
    private boolean openInterval;

    @Override
    public Boolean validate(T value) {
        if (value == null)
            return min == null && max == null;
        if (min != null && (openInterval ? (value.compareTo(min) < 0) : (value.compareTo(min) <= 0)))
            return false;
        if (max != null && (openInterval ? (value.compareTo(max) > 0) : (value.compareTo(max) >= 0)))
            return false;
        return true;
    }
}
