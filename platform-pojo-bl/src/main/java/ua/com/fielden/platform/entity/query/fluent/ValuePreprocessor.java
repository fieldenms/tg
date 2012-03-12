package ua.com.fielden.platform.entity.query.fluent;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.types.Money;

public class ValuePreprocessor {
    public Object preprocessValue(final Object value) {
	if (value != null && value.getClass().isArray()) {
	    final List<Object> values = new ArrayList<Object>();
	    for (final Object object : (Object[]) value) {
		values.add(convertValue(object));
	    }
	    return values.toArray();
	} else {
	    return convertValue(value);
	}
    }

    /** Ensures that values of special types such as {@link Class} or {@link PropertyDescriptor} are converted to String. */
    private Object convertValue(final Object value) {
	if (value instanceof AbstractEntity) {
	    return ((AbstractEntity<?>) value).getId();
	}

	if (value instanceof PropertyDescriptor || value instanceof Class) {
	    return value.toString();
	}

	if (value instanceof Money) {
	    return ((Money) value).getAmount();
	}

	return value;
    }
}
