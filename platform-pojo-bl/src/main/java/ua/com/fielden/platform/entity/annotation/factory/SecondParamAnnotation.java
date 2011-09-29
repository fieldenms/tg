package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.criteria.enhanced.SecondParam;


/**
 * A factory for convenient instantiation of {@link SecondParam} annotations, which mainly should be used for dynamic property creation.
 *
 * @author TG Team
 *
 */
public class SecondParamAnnotation {

    private final String firstParam;

    public SecondParamAnnotation(final String firstParam) {
	this.firstParam = firstParam;
    }

    public SecondParam newInstance() {
	return new SecondParam() {

	    @Override
	    public Class<SecondParam> annotationType() {
		return SecondParam.class;
	    }

	    @Override
	    public String firstParam() {
		return firstParam;
	    }


	};
    }
}
