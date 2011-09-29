package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.treemodel.rules.ICalculatedProperty.CalculatedPropertyCategory;


/**
 * A factory for convenient instantiation of {@link Calculated} annotations, which mainly should be used for dynamic property creation.
 *
 * @author TG Team
 *
 */
public class CalculatedAnnotation {

    private String expression = "";
    private String origination = "";
    private CalculatedPropertyCategory category = CalculatedPropertyCategory.EXPRESSION;


    public CalculatedAnnotation expression(final String expression) {
	this.expression = expression;
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
	    public String expression() {
		return expression;
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

    public Calculated copyFrom(final Calculated original) {
	return new Calculated() {

	    @Override
	    public Class<Calculated> annotationType() {
		return Calculated.class;
	    }

	    @Override
	    public String expression() {
		return original.expression();
	    }

	    @Override
	    public String origination() {
		return original.origination();
	    }

	    @Override
	    public CalculatedPropertyCategory category() {
		return original.category();
	    }

	};
    }
}
