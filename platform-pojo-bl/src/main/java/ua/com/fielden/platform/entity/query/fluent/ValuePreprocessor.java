package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.RichText;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import static ua.com.fielden.platform.utils.EntityUtils.*;

/**
 * This class is responsible for pre-processing of values passed as parameters to EQL queries (this covers both {@code .val()} and {@code .param()}).
 *
 * @author TG Team
 */
public class ValuePreprocessor {

    /**
     * @return  either a list of converted values or a single converted value
     */
    public Object apply(final Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Collection || value.getClass().isArray()) {
            final Stream<?> original = value instanceof Collection<?> collection
                    ? collection.stream()
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

    private Object convertValue(final Object value) {
        return switch (value) {
            case PropertyDescriptor<?> it -> it.toString();
            case Class<?>              it -> it.toString();
            case Colour                it -> it.toString();
            case Enum<?>               it -> it.toString();
            case Hyperlink             it -> it.toString();
            case DynamicEntityKey      it -> it.toString();
            case AbstractEntity<?> entity -> {
                final var type = entity.getType();
                yield entity.getId() == null && !(isPersistedEntityType(type) || isSyntheticEntityType(type) || isUnionEntityType(type))
                        ? entity.getKey() : entity.getId();
            }
            case Money money -> money.getAmount();
            case RichText richText -> richText.coreText();
            case null, default -> value;
        };
    }

}
