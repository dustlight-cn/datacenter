package cn.dustlight.datacenter.schema.adapter;


import com.networknt.schema.ValidationMessage;

import java.util.Set;

public class ValidationExceptionUtil {

    public static RuntimeException getException(Set<ValidationMessage> messages) {
        if (messages == null || messages.size() == 0)
            return null;
        StringBuilder builder = new StringBuilder();
        for (ValidationMessage message : messages) {
            if (builder.length() > 0)
                builder.append('\n');
            builder.append(message.getMessage());
        }
        return new RuntimeException(builder.toString());
    }

}
