package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.criteria.enhanced.CriteriaProperty;


/**
 * A factory for convenient instantiation of {@link CriteriaProperty} annotations, which mainly should be used for dynamic property creation.
 *
 * @author TG Team
 *
 */
public class CriteriaPropertyAnnotation {

    private final String propertyType;

    public CriteriaPropertyAnnotation(final String propertyType) {
	this.propertyType = propertyType;
    }

    public CriteriaProperty newInstance() {
	return new CriteriaProperty() {

	    @Override
	    public Class<CriteriaProperty> annotationType() {
		return CriteriaProperty.class;
	    }


	    @Override
	    public String propertyName() {
		return propertyType;
	    }

	};
    }

    public CriteriaProperty copyFrom(final CriteriaProperty original) {
	return new CriteriaProperty() {

	    @Override
	    public Class<CriteriaProperty> annotationType() {
		return CriteriaProperty.class;
	    }

	    @Override
	    public String propertyName() {
		return original.propertyName();
	    }

	};
    }
}
