package ua.com.fielden.platform.web.view.master.api.actions.property.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;
import ua.com.fielden.platform.web.view.master.api.actions.impl.AbstractAction;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * The implementation box for property actions.
 *
 * @author TG Team
 *
 */
public class PropertyAction extends AbstractAction implements IExecutable {
    /**
     * Creates {@link PropertyAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public PropertyAction(final String name, final String actionComponentPath, final Class<? extends AbstractEntity<?>> functionalEntityType, final IPreAction preAction, final IPostAction postActionSuccess, final IPostAction postActionError, final EnabledState enabledState, final String icon, final String shortDesc, final String longDesc) {
        super(name, actionComponentPath, functionalEntityType, preAction, postActionSuccess, postActionError, enabledState, icon, shortDesc, longDesc);
    }
}
