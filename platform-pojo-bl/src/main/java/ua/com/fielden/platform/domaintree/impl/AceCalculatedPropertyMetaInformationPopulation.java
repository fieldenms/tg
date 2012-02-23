package ua.com.fielden.platform.domaintree.impl;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

/**
 * A meta-information population definer after "contextualExpression" change of the {@link CalculatedProperty}.
 * It includes result type, calculated property place, category and other essentials.
 *
 * @author TG Team
 *
 */
public class AceCalculatedPropertyMetaInformationPopulation implements IAfterChangeEventHandler<Object> {

    @Override
    public void handle(final MetaProperty property, final Object entityPropertyValue) {
	final CalculatedProperty cp = (CalculatedProperty) property.getEntity();
	cp.inferMetaInformationFromExpression();
    }

}
