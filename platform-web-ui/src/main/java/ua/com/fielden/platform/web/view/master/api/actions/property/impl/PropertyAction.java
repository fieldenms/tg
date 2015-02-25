package ua.com.fielden.platform.web.view.master.api.actions.property.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.view.master.api.actions.impl.AbstractFunctionalAction;

/**
 * The implementation box for property actions.
 *
 * @author TG Team
 *
 */
public class PropertyAction extends AbstractFunctionalAction implements IExecutable {
    /**
     * Creates {@link PropertyAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public PropertyAction(final String name, final Class<? extends AbstractEntity<?>> functionalEntityType) {
        super(name, "master/actions/tg-property-action.html", functionalEntityType);
    }
}
