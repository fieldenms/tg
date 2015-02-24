package ua.com.fielden.platform.web.view.master.api.actions.entity.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;

/**
 * The implementation box for default entity actions (like 'Refresh', 'Edit' etc.).
 *
 * @author TG Team
 *
 */
public class DefaultEntityAction extends AbstractEntityAction {
    private final String onActionFunction;

    /**
     * Creates {@link DefaultEntityAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public DefaultEntityAction(final String name, final EnabledState enabledState, final String icon, final String shortDesc, final String longDesc, final String onActionFunction) {
        super(name, null, null, null, null, enabledState, icon, shortDesc, longDesc);
        this.onActionFunction = onActionFunction;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();

        final String actionSelector = "actions['" + this.name() + "']";

        attrs.put("action", "{{" + actionSelector + ".action}}");
        attrs.put("onAction", "{{" + this.onActionFunction() + "}}");

        return attrs;
    }

    private String onActionFunction() {
        return onActionFunction;
    }
}
