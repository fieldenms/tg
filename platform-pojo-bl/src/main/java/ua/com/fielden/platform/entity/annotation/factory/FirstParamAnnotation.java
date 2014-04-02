package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.criteria.enhanced.FirstParam;

/**
 * A factory for convenient instantiation of {@link FirstParam} annotations, which mainly should be used for dynamic property creation.
 * 
 * @author TG Team
 * 
 */
public class FirstParamAnnotation {

    private final String secondParam;

    public FirstParamAnnotation(final String secondParam) {
        this.secondParam = secondParam;
    }

    public FirstParam newInstance() {
        return new FirstParam() {

            @Override
            public Class<FirstParam> annotationType() {
                return FirstParam.class;
            }

            @Override
            public String secondParam() {
                return secondParam;
            }

        };
    }
}
