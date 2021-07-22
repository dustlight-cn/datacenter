package plus.datacenter.elasticsearch.utils;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class OrderUtil {

    public static Sort.Order getOrder(String property) {
        if (property == null)
            return null;
        if (property.startsWith("-"))
            return Sort.Order.desc(property.substring(1));
        return Sort.Order.asc(property);
    }

    public static Sort.Order[] getOrders(String... properties) {
        if (properties == null)
            return null;
        Sort.Order[] result = new Sort.Order[properties.length];
        for (int i = 0, len = result.length; i < len; i++)
            result[i] = getOrder(properties[i]);
        return result;
    }

    public static List<Sort.Order> getOrders(List<String> properties) {
        if (properties == null)
            return null;
        List<Sort.Order> result = new ArrayList<>(properties.size());
        for (String property : properties)
            result.add(getOrder(property));
        return result;
    }
}
