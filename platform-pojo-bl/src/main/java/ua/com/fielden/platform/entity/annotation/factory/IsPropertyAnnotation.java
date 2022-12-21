package ua.com.fielden.platform.entity.annotation.factory;

import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_DISPLAY_AS;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_LENGTH;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_LINK_PROPERTY;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_PRECISION;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_SCALE;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_TRAILING_ZEROS;

import ua.com.fielden.platform.entity.annotation.IsProperty;

/**
 * A factory for convenient instantiation of {@link IsProperty} annotations, which mainly should be used for dynamic property creation.
 *
 * @author TG Team
 *
 */
public class IsPropertyAnnotation {
    private Class<?> value = Void.class;
    private final boolean assignBeforeSave;
    private final String linkProperty;

    private final int length;
    private final int precision;
    private final int scale;
    private final boolean trailingZeros;
    private final String displayAs;

    public IsPropertyAnnotation(
            final Class<?> value,
            final String linkProperty,
            final boolean assignBeforeSave,
            final int length,
            final int precision,
            final int scale,
            final boolean trailingZeros,
            final String displayAs) {
        this.value = value;
        this.linkProperty = linkProperty;
        this.assignBeforeSave = assignBeforeSave;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
        this.trailingZeros = trailingZeros;
        this.displayAs = displayAs;
    }

    public IsPropertyAnnotation(final Class<?> value, final String linkProperty, final boolean assignBeforeSave) {
        this(value, linkProperty, assignBeforeSave, DEFAULT_LENGTH, DEFAULT_PRECISION, DEFAULT_SCALE, DEFAULT_TRAILING_ZEROS, DEFAULT_DISPLAY_AS);
    }

    public IsPropertyAnnotation(final int precision, final int scale) {
        this(Void.class, DEFAULT_LINK_PROPERTY, false, DEFAULT_LENGTH, precision, scale, DEFAULT_TRAILING_ZEROS, DEFAULT_DISPLAY_AS);
    }

    public IsPropertyAnnotation(final Class<?> value, final String linkProperty) {
        this(value, linkProperty, false);
    }

    public IsPropertyAnnotation(final Class<?> value) {
        this(value, DEFAULT_LINK_PROPERTY, false);
    }

    public IsPropertyAnnotation() {
        this(Void.class, DEFAULT_LINK_PROPERTY, false);
    }

    public IsPropertyAnnotation value(final Class<?> value) {
        this.value = value;
        return this;
    }
    
    private static IsProperty create(final Class<?> value, final String linkProperty, final boolean assignBeforeSave,
            final int length, final int precision, final int scale, final boolean trailingZeros, final String displayAs) 
    {
        return new IsProperty() {
            @Override public Class<IsProperty> annotationType() { return IsProperty.class; }
            @Override public Class<?> value() { return value; }
            @Override public String linkProperty() { return linkProperty; }
            @Override public boolean assignBeforeSave() { return assignBeforeSave; }
            @Override public int length() { return length; }
            @Override public int precision() { return precision; }
            @Override public int scale() { return scale; }
            @Override public boolean trailingZeros() { return trailingZeros; }
            @Override public String displayAs() { return displayAs; }
            
            @Override
            public boolean equals(final Object other) {
                if (this == other) {
                    return true;
                }
                return (other instanceof IsProperty atIsProp) &&
                        this.annotationType().equals(atIsProp.annotationType()) &&
                        this.value().equals(atIsProp.value()) &&
                        this.linkProperty().equals(atIsProp.linkProperty()) &&
                        this.assignBeforeSave() == atIsProp.assignBeforeSave() &&
                        this.length() == atIsProp.length() &&
                        this.precision() == atIsProp.precision() &&
                        this.scale() == atIsProp.scale() &&
                        this.trailingZeros() == atIsProp.trailingZeros() &&
                        this.displayAs().equals(atIsProp.displayAs());
            }
        };
    }

    public IsProperty newInstance() {
        return create(value, linkProperty, assignBeforeSave, length, precision, scale, trailingZeros, displayAs);
    }

    public IsProperty copyFrom(final IsProperty original) {
        return from(original).newInstance();
    }
    
    /**
     * Creates a new instance based on the given {@link IsProperty} annotation.
     * <p>
     * This method differs from {@link #copyFrom(IsProperty)} in that it returns {@link IsPropertyAnnotation} which allows further modification of <code>original</code>.
     * <p>
     * For example:
     * <pre>
     * IsPropertyAnnotation.from(original).value(String.class).newInstance();
     * </pre>
     * @param original
     * @return
     */
    public static IsPropertyAnnotation from(final IsProperty original) {
        return new IsPropertyAnnotation(original.value(), original.linkProperty(), original.assignBeforeSave(), original.length(),
                original.precision(), original.scale(), original.trailingZeros(), original.displayAs());
    }
}
