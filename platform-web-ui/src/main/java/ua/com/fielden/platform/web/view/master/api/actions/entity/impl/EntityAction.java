package ua.com.fielden.platform.web.view.master.api.actions.entity.impl;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;
import ua.com.fielden.platform.web.view.master.api.actions.impl.AbstractFunctionalAction;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

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
    public EntityAction(final String name, final Class<? extends AbstractEntity<?>> functionalEntityType, final IPreAction preAction, final IPostAction postActionSuccess, final IPostAction postActionError, final EnabledState enabledState, final String icon, final String shortDesc, final String longDesc) {
        super(name, "master/actions/tg-entity-action.html", functionalEntityType, preAction, postActionSuccess, postActionError, enabledState, icon, shortDesc, longDesc);
    }

    @Override
    public DomElement render() {
        return new DomElement(this.actionComponentName()).attrs(createAttributes()).attrs(createCustomAttributes());
    }
}
