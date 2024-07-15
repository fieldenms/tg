package ua.com.fielden.platform.entity.query.fluent;

import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;

/**
 * This class is responsible for pre-processing of values passed as parameters to EQL queries (this covers both {@code .val()} and {@code .param()}).
 *
 * @author TG Team
 *
 */
public class ValuePreprocessor {

    public Object apply(final Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Collection || value.getClass().isArray()) {
            final List<Object> result = new ArrayList<Object>();
            final Collection<Object> original = value instanceof Collection ? (Collection<Object>) value : Arrays.asList((Object[]) value);
            for (final Object object : original) {
                result.add(convertValue(object));
            }
            return result;
        } else {
            return convertValue(value);
        }
    }

    /** Ensures that values of special types such as {@link Class} or {@link PropertyDescriptor} are converted to String. */
    private Object convertValue(final Object value) {
        final Object result;
        if (value instanceof PropertyDescriptor ||
            value instanceof Class ||
            value instanceof Colour ||
            value instanceof Enum ||
            value instanceof Hyperlink ||
            value instanceof DynamicEntityKey) {
            result = value.toString();
        } else if (value instanceof AbstractEntity) {
            final AbstractEntity<?> entity = (AbstractEntity<?>) value;
            final Class<? extends AbstractEntity<?>> type = entity.getType();
            result = entity.getId() == null && !(isPersistedEntityType(type) || isSyntheticEntityType(type) || isUnionEntityType(type)) ? entity.getKey() : entity.getId();
        } else if (value instanceof Money) {
            result = ((Money) value).getAmount();
        } else {
            result = value;
        }
        return result;
    }
}