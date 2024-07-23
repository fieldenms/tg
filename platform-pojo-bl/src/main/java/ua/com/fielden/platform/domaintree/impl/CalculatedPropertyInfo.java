package ua.com.fielden.platform.domaintree.impl;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;

public record CalculatedPropertyInfo (
        Class<?> root,
        String contextPath,
        String customPropertyName,
        String contextualExpression,
        String title,
        CalculatedPropertyAttribute attribute,
        String originationProperty,
        String desc,
        Integer precision,
        Integer scale) {
}