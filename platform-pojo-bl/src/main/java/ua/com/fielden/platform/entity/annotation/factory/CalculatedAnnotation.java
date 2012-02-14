package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.entity.annotation.Calculated;


/**
 * A factory for convenient instantiation of {@link Calculated} annotations, which mainly should be used for dynamic property creation.
 *
 * @author TG Team
 *
 */
public class CalculatedAnnotation {
    private String contextualExpression = "";
    private String contextPath = "";
    private CalculatedPropertyAttribute attribute = CalculatedPropertyAttribute.NO_ATTR;
    private String origination = "";
    private CalculatedPropertyCategory category = CalculatedPropertyCategory.EXPRESSION;

    public CalculatedAnnotation contextualExpression(final String contextualExpression) {
	this.contextualExpression = contextualExpression;
	return this;
    }

    public CalculatedAnnotation contextPath(final String contextPath) {
	this.contextPath = contextPath;
	return this;
    }

    public CalculatedAnnotation attribute(final CalculatedPropertyAttribute attribute) {
	this.attribute = attribute;
	return this;
    }

    public CalculatedAnnotation origination(final String origination) {
	this.origination = origination;
	return this;
    }

    public CalculatedAnnotation category(final CalculatedPropertyCategory category) {
	this.category = category;
	return this;
    }

    public Calculated newInstance() {
	return new Calculated() {

	    @Override
	    public Class<Calculated> annotationType() {
		return Calculated.class;
	    }

	    @Override
	    public String contextualExpression() {
		return contextualExpression;
	    }

	    @Override
	    public String contextPath() {
	        return contextPath;
	    }

	    @Override
	    public CalculatedPropertyAttribute attribute() {
	        return attribute;
	    }

	    @Override
	    public String origination() {
		return origination;
	    }

	    @Override
	    public CalculatedPropertyCategory category() {
	        return category;
	    }
	};
    }

//    public Calculated copyFrom(final Calculated original) {
//	return new Calculated() {
//
//	    @Override
//	    public Class<Calculated> annotationType() {
//		return Calculated.class;
//	    }
//
//	    @Override
//	    public String contextualExpression() {
//		return original.contextualExpression();
//	    }
//
//	    @Override
//	    public String origination() {
//		return original.origination();
//	    }
//
//	    @Override
//	    public CalculatedPropertyCategory category() {
//		return original.category();
//	    }
//
//	};
//    }
}
