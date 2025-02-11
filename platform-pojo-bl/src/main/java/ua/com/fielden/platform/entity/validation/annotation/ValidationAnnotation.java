package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.Annotation;

import ua.com.fielden.platform.entity.annotation.Dynamic;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Unique;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;

/**
 * Defines all annotation types, which represent validation annotations.
 *
 * The order of enumeration values defines validation priority,
 *
 * @author TG Team
 *
 */
public enum ValidationAnnotation {
    REQUIRED(Required.class), // required and not null are similar in nature, but required represents a dynamic validation
    FINAL(Final.class), //
    ENTITY_EXISTS(EntityExists.class), //
    GREATER_OR_EQUAL(GreaterOrEqual.class), //
    GE_PROPETY(GeProperty.class), //
    LE_PROPETY(LeProperty.class), //
    MAX(Max.class), //
    DOMAIN(DomainValidation.class), //
    BEFORE_CHANGE(BeforeChange.class), //
    UNIQUE(Unique.class),
    /** Represents an unhandled error that occured during the processing of raw property values. */
    UNHANDLED_ERROR(UnhandledErrorValidation.class),
    DYNAMIC(Dynamic.class); // DYNAMIC validation logic is encapsulated inside the setters.

    private final Class<? extends Annotation> type;

    public Class<? extends Annotation> getType() {
        return type;
    }

    /**
     * Looks for enumeration value associated with the specified validation annotation type.
     *
     * @param annotation
     * @return
     */
    public static ValidationAnnotation getValueByType(final Annotation annotation) {
        for (final ValidationAnnotation value : values()) {
            if (value.getType() == annotation.annotationType()) {
                return value;
            }
        }
        return null;
    }

    ValidationAnnotation(final Class<? extends Annotation> type) {
        this.type = type;
    }
}
