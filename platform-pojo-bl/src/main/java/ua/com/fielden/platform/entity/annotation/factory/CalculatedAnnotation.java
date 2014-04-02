package ua.com.fielden.platform.entity.annotation.factory;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.error.Result;

/**
 * A factory for convenient instantiation of {@link Calculated} annotations, which mainly should be used for dynamic property creation.
 * 
 * @author TG Team
 * 
 */
public class CalculatedAnnotation {
    private String contextualExpression = "";
    private String rootTypeName = Calculated.NOTHING;
    private String contextPath = Calculated.NOTHING;
    private CalculatedPropertyAttribute attribute = CalculatedPropertyAttribute.NO_ATTR;
    private String origination = Calculated.NOTHING;
    private CalculatedPropertyCategory category = CalculatedPropertyCategory.EXPRESSION;

    public CalculatedAnnotation contextualExpression(final String contextualExpression) {
        if (StringUtils.isEmpty(contextualExpression)) {
            throw new Result("The calculated property 'contextualExpression' cannot be 'null' or empty.");
        }
        this.contextualExpression = contextualExpression;
        return this;
    }

    public CalculatedAnnotation rootTypeName(final String rootTypeName) {
        if (StringUtils.isEmpty(rootTypeName)) {
            throw new Result("The calculated property 'root' cannot be 'null' or empty.");
        }
        this.rootTypeName = rootTypeName;
        return this;
    }

    public CalculatedAnnotation contextPath(final String contextPath) {
        if (contextPath == null) {
            throw new Result("The calculated property 'contexPath' cannot be 'null'.");
        }
        this.contextPath = contextPath;
        return this;
    }

    public CalculatedAnnotation attribute(final CalculatedPropertyAttribute attribute) {
        if (attribute == null) {
            throw new Result("The calculated property 'attribute' cannot be 'null'.");
        }
        this.attribute = attribute;
        return this;
    }

    public CalculatedAnnotation origination(final String origination) {
        if (origination == null) {
            throw new Result("The calculated property 'origination' cannot be 'null'.");
        }
        this.origination = origination;
        return this;
    }

    public CalculatedAnnotation category(final CalculatedPropertyCategory category) {
        if (category == null) {
            throw new Result("The calculated property 'category' cannot be 'null'.");
        }
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
            public String value() {
                return contextualExpression;
            }

            @Override
            public String rootTypeName() {
                return rootTypeName;
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
}
