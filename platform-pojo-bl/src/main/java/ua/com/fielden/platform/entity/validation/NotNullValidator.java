package ua.com.fielden.platform.entity.validation;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.error.Result;

/**
 * Checks value for null and empty in case where new value is instance of String.
 *
 * @author TG Team
 *
 */
public class NotNullValidator implements IBeforeChangeEventHandler<Object> {

    /**
     * Validation message, which can be set as part of BCE handler declaration.
     */
    private String validationMsg;

    public NotNullValidator() {
    }

    @Override
    public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
        final Object entity = property.getEntity();
        final NotNull notNull = findNotNullAnnotation(mutatorAnnotations);
        final String msg = "<html>Null or empty value is not permitted for property <b>" + property.getName() + "</b></html>";
        final String errorMsg = notNull != null ? (!isEmpty(notNull.value()) ? notNull.value() : msg) : (!isEmpty(validationMsg) ? validationMsg : msg);

        return isNull(newValue, oldValue) //
        ? new Result(entity, new IllegalArgumentException(errorMsg)) //
                : new Result(entity, "Value " + newValue + " is valid for property " + property.getName());
    }

    /**
     * Convenient method to determine if the newValue is "null" or is empty in terms of value.
     *
     * @param newValue
     * @param oldValue
     * @return
     */
    private boolean isNull(final Object newValue, final Object oldValue) {
        // IMPORTANT : need to check NotNullValidator usage on existing logic. There is the case, when
        // should not to pass the validation : setRotable(null) in AdvicePosition when getRotable() == null!!!
        // that is why - - "&& (oldValue != null)" - - was removed!!!!!
        // The current condition is essential for UI binding logic.
        return (newValue == null) /* && (oldValue != null) */
                || (newValue instanceof String && StringUtils.isEmpty(newValue.toString()) && !StringUtils.isEmpty((String) oldValue));
    }

    /**
     * A convenient method to find NotNull annotation instance in the list of annotations be class.
     *
     * @param mutatorAnnotations
     * @return
     */
    private NotNull findNotNullAnnotation(final Set<Annotation> mutatorAnnotations) {
        for (final Annotation annot : mutatorAnnotations) {
            if (NotNull.class == annot.annotationType()) {
                return (NotNull) annot;
            }
        }
        return null;
    }
}
