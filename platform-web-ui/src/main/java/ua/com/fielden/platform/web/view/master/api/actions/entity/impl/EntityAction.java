package ua.com.fielden.platform.web.view.master.api.actions.entity.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * The implementation box for entity actions.
 *
 * @author TG Team
 *
 */
public class EntityAction extends AbstractEntityAction {
    /**
     * Creates {@link EntityAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public EntityAction(final String name, final Class<? extends AbstractEntity<?>> functionalEntityType, final IPreAction preAction, final IPostAction postActionSuccess, final IPostAction postActionError, final EnabledState enabledState, final String icon, final String shortDesc, final String longDesc) {
        super(name, functionalEntityType, preAction, postActionSuccess, postActionError, enabledState, icon, shortDesc, longDesc);
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();

        final String actionSelector = "actions['" + this.name() + "']";

        attrs.put("preAction", "{{" + actionSelector + ".preAction}}");
        attrs.put("postActionSuccess", "{{" + actionSelector + ".postActionSuccess}}");
        attrs.put("postActionError", "{{" + actionSelector + ".postActionError}}");

        return attrs;
    }
}
