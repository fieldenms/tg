package ua.com.fielden.platform.entity.validation;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.RichText;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

import static java.lang.Math.min;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;

/**
 * This validator implements a check for the length of values for properties of type {@link String}, {@link Hyperlink}, and {@link RichText}.
 * Every non-calculated property of type `String`, defined with `length > 0`, attains this validator by default, which will use the defined length.
 * <p>
 * If the attempted value exceeds the limit, then it is rejected.
 * Empty values are accepted with success.
 * <p>
 * This validator can also be specified explicitly.
 * The limit parameter for max length can be specified as part of a property definition.
 * {@snippet :
   @IsProperty
   @BeforeChange(@Handler(value = MaxLengthValidator.class, integer = @IntParam(name = "limit", value = 5)))
   private String name;
 * }
 * However, a better approach is to align the limit with the length in the property definition, which happens automagically if {@code length} for {@code @IsProperty}
 * is specified and {@code limit} is omitted completely for {@code @Handler(MaxLengthValidator.class)}.
 * {@snippet :
   @IsProperty(length = 5)
   @BeforeChange(@Handler(MaxLengthValidator.class))
   private String name;
 * }
 * In case of neither {@code limit} nor {@code length} defined, validation always returns a failure.
 *
 * @author TG Team
 */
public class MaxLengthValidator implements IBeforeChangeEventHandler<Object> {
    public static final String ERR_MISSING_MAX_LENGTH = "No max length was specified.";
    public static final String ERR_VALUE_SHOULD_NOT_EXCEED_MAX_LENGTH = "Value should not be longer than %s characters.";
    public static final String ERR_UNSUPPORTED_PROPERTY_TYPE = "Validator [%s] is not applicable to properties of type [%s].";

    public static final Set<Class<?>> SUPPORTED_TYPES = Set.of(String.class, Hyperlink.class, RichText.class);

    private Integer limit;

    protected MaxLengthValidator() { }

    public MaxLengthValidator(final Integer limit) {
        this.limit = limit;
    }

    @Override
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return successful();
        }

        if (!SUPPORTED_TYPES.contains(newValue.getClass())) {
            throw new EntityDefinitionException(ERR_UNSUPPORTED_PROPERTY_TYPE.formatted(MaxLengthValidator.class.getSimpleName(), property.getType().getSimpleName()));
        }

        final var maybeLimit = determineMaxLength(property);
        if (maybeLimit.isEmpty()) {
            return failure(ERR_MISSING_MAX_LENGTH);
        }
        else if (maybeLimit.get() == IsProperty.MAX_LENGTH) {
            return  successful();
        }
        else {
            return length(newValue) > maybeLimit.get()
                    ? failure(property.getEntity(), ERR_VALUE_SHOULD_NOT_EXCEED_MAX_LENGTH.formatted(maybeLimit.get()))
                    : successful();
        }
    }

    /**
     * Attempts to determine the max length limit by using both the {@code limit} parameter for {@code MaxLengthValidator} and the {@code length} for {@code IsProperty}.
     * The smaller of the two is returned or an empty value if none are present.
     * Choosing the smaller limit is considered to be a safer option.
     *
     * @param property
     * @return
     */
    private Optional<Integer> determineMaxLength(final MetaProperty<Object> property) {
        // the highest preference is for the explicitly specified limit parameter
        final Optional<Integer> maybeLimit = ofNullable(limit);
        // alternatively, try to determine the limit from the property definition
        final IsProperty anProp = getPropertyAnnotation(IsProperty.class, property.getEntity().getClass(), property.getName());
        final Optional<Integer> maybeLength = ofNullable(anProp.length() > IsProperty.DEFAULT_LENGTH ? anProp.length() : null);
        // get a minimum of limit and length if both or either is present, otherwise an empty result would be returned
        return maybeLimit.map(limit -> maybeLength.map(length -> min(limit, length)).orElse(limit)).or(() -> maybeLength);
    }

    private static int length(Object value) {
        return switch (value) {
            case null -> 0;
            case String s -> s.length();
            case Hyperlink hyperlink -> hyperlink.value.length();
            case RichText richText -> RichText.makeSearchText(richText).length();
            default -> throw new EntityDefinitionException(ERR_UNSUPPORTED_PROPERTY_TYPE.formatted(MaxLengthValidator.class.getSimpleName(), value.getClass().getSimpleName()));
        };

    }
}
