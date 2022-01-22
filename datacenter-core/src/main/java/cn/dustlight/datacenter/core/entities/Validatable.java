package cn.dustlight.datacenter.core.entities;

public interface Validatable<T> {

    Boolean validate(T value);

}
