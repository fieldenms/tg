package ua.com.fielden.platform.entity.validation;

import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Hyperlink;

/**
 * This validator implements a check for the length of a string property.
 * If the attempted value exceeds the limit then it is rejected.
 * Empty values are accepted with success.
 * <p>
 * The limit parameter for max length can be specified specified as part of a property definition.
 * 
 * <pre>
 *  &#64;IsProperty
 *  &#64;BeforeChange(&#64;Handler(value = MaxLengthValidator.class, integer = &#64;IntParam(name = "limit", value = 5)))
 *  private String name;
 * </pre>
 * <p>
 * However, a better approach is to align the limit with the length in the property definition, which happens automagically if {@code length} for {@code @IsProperty}
 * is specified and {@code limit} is omitted completely for {@code @Handler(MaxLengthValidator.class)}.
 * <pre>
 *  &#64;IsProperty(length = 5)
 *  &#64;BeforeChange(&#64;Handler(MaxLengthValidator.class))
 *  private String name;
 * </pre>
 * <p>
 * In case of neither {@code limit} nor {@code length} defined, validation always returns a failure.
 *
 * @author TG Air
 *
 */
public class MaxLengthValidator implements IBeforeChangeEventHandler<Object> {
    public static final String ERR_MISSING_MAX_LENGTH = "No max length was specified.";
    public static final String ERR_VALUE_SHOULD_NOT_EXCEED_MAX_LENGTH = "Value should not be longer than %s characters.";
    public static final String ERR_UNSUPPORTED_PROPERTY_TYPE = "Validator [%s] is not applicable to properties of type [%s].";

    private Integer limit;

    protected MaxLengthValidator() { }

    public MaxLengthValidator(final Integer limit) {
        this.limit = limit;
    }

    @Override
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return successful("Value is empty.");
        }
        if (!(newValue instanceof String) && !(newValue instanceof Hyperlink)) {
            throw new EntityDefinitionException(format(ERR_UNSUPPORTED_PROPERTY_TYPE, MaxLengthValidator.class.getSimpleName(), newValue.getClass().getSimpleName()));
        }
        return determineMaxLength(property).map(maxLength
                -> 
                    length(newValue) > maxLength
                    ? failure(property.getEntity(), format(ERR_VALUE_SHOULD_NOT_EXCEED_MAX_LENGTH, maxLength))
                    : successful(property.getEntity()))
                .orElse(failure(ERR_MISSING_MAX_LENGTH));
    }

    /**
     * Attempts to determine the max length limit by using both the {@code limit} parameter for {@code MaxLengthValidator} and the {@code length} for {@code IsProperty}.
     * The smaller of the two is returned or an empty value if none are present.
     * Choosing the smaller limit is considered to be a safer option.
     *
     * @param property
     * @param limit
     * @return
     */
    private Optional<Integer> determineMaxLength(final MetaProperty<Object> property) {
        // the highest preference is for the explicitly specified limit parameter
        final Optional<Integer> maybeLimit = ofNullable(limit);
        // alternatively try to determine the limit from the property definition
        final IsProperty anProp = getPropertyAnnotation(IsProperty.class, property.getEntity().getClass(), property.getName());
        final Optional<Integer> maybeLength = ofNullable(anProp.length() > IsProperty.DEFAULT_LENGTH ? anProp.length() : null);
        // get a minimum of limit and length if both or either are present, otherwise an empty result would be returned
        return maybeLimit.map( limit -> of(maybeLength.map(length -> min(limit, length)).orElse(limit)) ).orElse(maybeLength);
    }

    private static int length(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof String) {
            return ((String) value).length();
        }
        
        if (value instanceof Hyperlink) {
            return ((Hyperlink) value).value.length();
        }
        throw new EntityDefinitionException(format(ERR_UNSUPPORTED_PROPERTY_TYPE, MaxLengthValidator.class.getSimpleName(), value.getClass().getSimpleName()));
    }
}