package ua.com.fielden.platform.domaintree.impl;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

/**
 * A "name" population definer after "title" change of the {@link CalculatedProperty}.
 *
 * @author TG Team
 *
 */
public class AceCalculatedPropertyNamePopulation implements IAfterChangeEventHandler<String> {

    @Override
    public void handle(final MetaProperty property, final String title) {
	final CalculatedProperty calculatedProperty = (CalculatedProperty) property.getEntity();
	calculatedProperty.setName(CalculatedProperty.generateNameFrom(title));
    }

}
