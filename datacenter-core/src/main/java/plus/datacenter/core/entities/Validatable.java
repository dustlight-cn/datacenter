package plus.datacenter.core.entities;

public interface Validatable<T> {

    Boolean validate(T value);

}
