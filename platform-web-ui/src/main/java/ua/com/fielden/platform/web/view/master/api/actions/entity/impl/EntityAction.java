package ua.com.fielden.platform.web.view.master.api.actions.entity.impl;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.actions.impl.AbstractFunctionalAction;

/**
 * The implementation box for entity actions.
 *
 * @author TG Team
 *
 */
public class EntityAction extends AbstractFunctionalAction implements IRenderable, IExecutable {
    /**
     * Creates {@link EntityAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public EntityAction(final String name, final Class<? extends AbstractEntity<?>> functionalEntityType) {
        super(name, "master/actions/tg-entity-action", functionalEntityType);
    }

    @Override
    public DomElement render() {
        return new DomElement(this.actionComponentName()).attrs(createAttributes()).attrs(createCustomAttributes());
    }
}
