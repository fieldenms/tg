/**
 *
 */
package ua.com.fielden.platform.entity.validation;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.annotation.NotEmpty;
import ua.com.fielden.platform.error.Result;

/**
 * Validator for setters marked with {@link NotEmpty} annotation. Checks whether new value's {@link #toString()} representation is not empty (empty means comparing with "" string,
 * null is treated as correct value).
 * 
 * @author Yura
 */
public class NotEmptyValidator implements IBeforeChangeEventHandler<Object> {

    @Override
    public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
        final Object entity = property.getEntity();
        if (newValue != null && "".equals(newValue.toString())) {
            final NotEmpty annotation = findNotEmptyAnnotation(mutatorAnnotations);
            final String errorMsg = !isEmpty(annotation.value()) ? annotation.value() : "<html>Empty value is not permitted for property <b>" + property.getName() + "</b></html>";
            return new Result(entity, new EmptyValueException(errorMsg));
        } else {
            return successful(entity);
        }
    }

    /**
     * A convenient method to find NotEmpty annotation instance in the list of annotations be class.
     * 
     * @param mutatorAnnotations
     * @return
     */
    private NotEmpty findNotEmptyAnnotation(final Set<Annotation> mutatorAnnotations) {
        for (final Annotation annot : mutatorAnnotations) {
            if (NotEmpty.class == annot.annotationType()) {
                return (NotEmpty) annot;
            }
        }
        return null;
    }

    public static class EmptyValueException extends Exception {
        private static final long serialVersionUID = -6345161881975049911L;

        public EmptyValueException(final String message) {
            super(message);
        }
    }

}
