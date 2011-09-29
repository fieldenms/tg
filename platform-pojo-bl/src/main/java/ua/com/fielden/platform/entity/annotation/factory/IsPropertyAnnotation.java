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

    public IsPropertyAnnotation(final Class<?> value) {
	this.value = value;
    }

    public IsPropertyAnnotation() {
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
	};
    }
}
