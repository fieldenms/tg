package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import static ua.com.fielden.platform.utils.EntityUtils.*;

/**
 * This class is responsible for pre-processing of values passed as parameters to EQL queries (this covers both {@code .val()} and {@code .param()}).
 *
 * @author TG Team
 *
 */
public class ValuePreprocessor {

    /**
     * @return  either a list of converted values or a single converted value
     */
    public Object apply(final Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Collection || value.getClass().isArray()) {
            final Stream<Object> original = value instanceof Collection
                    ? ((Collection<Object>) value).stream()
                    : Arrays.stream((Object[]) value);
            return apply(original).toList();
        } else {
            return convertValue(value);
        }
    }

    public Stream<Object> applyMany(final Object... values) {
        return apply(Arrays.stream(values));
    }

    public Stream<Object> apply(final Stream<?> values) {
        return values.map(this::convertValue);
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
