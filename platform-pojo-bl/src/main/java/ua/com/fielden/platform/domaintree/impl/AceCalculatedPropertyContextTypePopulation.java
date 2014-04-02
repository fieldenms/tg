package ua.com.fielden.platform.domaintree.impl;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

/**
 * A "contextType" population definer after "contextPath" change of the {@link CalculatedProperty}.
 * 
 * @author TG Team
 * 
 */
public class AceCalculatedPropertyContextTypePopulation implements IAfterChangeEventHandler<String> {
    @Override
    public void handle(final MetaProperty property, final String title) {
        ((CalculatedProperty) property.getEntity()).inferContextType();
    }
}
