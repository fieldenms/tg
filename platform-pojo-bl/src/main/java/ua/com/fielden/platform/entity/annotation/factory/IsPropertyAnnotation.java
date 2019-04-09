package ua.com.fielden.platform.entity.annotation.factory;

import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_LENGTH;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_PRECISION;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_SCALE;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_LINK_PROPERTY;

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

    public IsPropertyAnnotation(
            final Class<?> value, 
            final String linkProperty, 
            final boolean assignBeforeSave,
            final int length,
            final int precision,
            final int scale,
            final boolean trailingZeros) {
        this.value = value;
        this.linkProperty = linkProperty;
        this.assignBeforeSave = assignBeforeSave;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
        this.trailingZeros = trailingZeros;
    }

    public IsPropertyAnnotation(final Class<?> value, final String linkProperty, final boolean assignBeforeSave) {
        this(value, linkProperty, assignBeforeSave, DEFAULT_LENGTH, DEFAULT_PRECISION, DEFAULT_SCALE, IsProperty.DEFAULT_TRAILING_ZEROS);
    }

    public IsPropertyAnnotation(final Class<?> value, final int precision, final int scale) {
        this(Void.class, DEFAULT_LINK_PROPERTY, false, DEFAULT_LENGTH, precision, scale, IsProperty.DEFAULT_TRAILING_ZEROS);
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

    public IsProperty newInstance() {
        return new IsProperty() {

            @Override
            public Class<IsProperty> annotationType() {
                return IsProperty.class;
            }

            @Override
            public Class<?> value() {
                return value;
            }

            @Override
            public String linkProperty() {
                return linkProperty;
            }

            @Override
            public boolean assignBeforeSave() {
                return assignBeforeSave;
            }

            @Override
            public int length() {
                return length;
            }

            @Override
            public int precision() {
                return precision;
            }

            @Override
            public int scale() {
                return scale;
            }
            
            @Override
            public boolean trailingZeros() {
                return trailingZeros;
            }
        };
    }

    public IsProperty copyFrom(final IsProperty original) {
        return new IsProperty() {

            @Override
            public Class<IsProperty> annotationType() {
                return IsProperty.class;
            }

            @Override
            public Class<?> value() {
                return original.value();
            }

            @Override
            public String linkProperty() {
                return original.linkProperty();
            }

            @Override
            public boolean assignBeforeSave() {
                return original.assignBeforeSave();
            }

            @Override
            public int length() {
                return original.length();
            }

            @Override
            public int precision() {
                return original.precision();
            }

            @Override
            public int scale() {
                return original.scale();
            }

            @Override
            public boolean trailingZeros() {
                return original.trailingZeros();
            }
        };
    }
}
