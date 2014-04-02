package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.entity.annotation.IsProperty;

/**
 * A factory for convenient instantiation of {@link IsProperty} annotations, which mainly should be used for dynamic property creation.
 * 
 * @author TG Team
 * 
 */
public class IsPropertyAnnotation {
    private Class<?> value = Void.class;
    private final String linkProperty;

    public IsPropertyAnnotation(final Class<?> value, final String linkProperty) {
        this.value = value;
        this.linkProperty = linkProperty;
    }

    public IsPropertyAnnotation(final Class<?> value) {
        this(value, "----dummy-property----");
    }

    public IsPropertyAnnotation() {
        this(Void.class, "----dummy-property----");
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
                return new String(linkProperty);
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
                return new String(original.linkProperty());
            }
        };
    }
}
