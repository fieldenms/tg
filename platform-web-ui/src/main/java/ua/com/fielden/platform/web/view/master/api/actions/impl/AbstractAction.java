package ua.com.fielden.platform.web.view.master.api.actions.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * The base implementation box for generic information for all actions.
 *
 * The information includes <code>enabledWhen</code> parameter, <code>shortDesc</code> etc.
 *
 * All action implementations should be based on this one and should be extended by action-specific configuration data.
 *
 * @author TG Team
 *
 */
public abstract class AbstractAction {
    private final String name;
    private final EnabledState enabledState;
    private final String icon;
    private final String shortDesc;
    private final String longDesc;
    private final String actionComponentName;
    private final String actionComponentPath;

    /**
     * Creates {@link AbstractAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public AbstractAction(final String name, final String actionComponentPath, final EnabledState enabledState, final String icon, final String shortDesc, final String longDesc) {
        this.name = name;
        this.actionComponentName = AbstractWidget.extractNameFrom(actionComponentPath);
        this.actionComponentPath = actionComponentPath;
        this.enabledState = enabledState;
        this.icon = icon;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
    }

    protected String name() {
        return name;
    }

    protected EnabledState enabledState() {
        return enabledState;
    }

    protected String icon() {
        return icon;
    }

    protected String shortDesc() {
        return shortDesc;
    }

    protected String longDesc() {
        return longDesc;
    }

    protected String actionComponentName() {
        return actionComponentName;
    }

    protected String actionComponentPath() {
        return actionComponentPath;
    }

    protected String enabledStatesString() {
        return EnabledState.ANY.equals(this.enabledState) ? "'EDIT', 'VIEW'" :
                EnabledState.EDIT.equals(this.enabledState) ? "'EDIT'" :
                        EnabledState.VIEW.equals(this.enabledState) ? "'VIEW'" : "'UNDEFINED'";
    }

    /**
     * Creates an attributes that will be used for entity action component generation.
     * <p>
     * Please, implement this method in descendants (for concrete entity actions) to extend the attributes set by action-specific attributes.
     *
     * @return
     */
    protected abstract Map<String, Object> createCustomAttributes();

    /**
     * Creates an attributes that will be used for entity action component generation.
     *
     * @return
     */
    protected Map<String, Object> createAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();

        final String actionSelector = "actions['" + this.name() + "']";
        attrs.put("user", "{{" + actionSelector + ".user}}");
        attrs.put("entitytype", "{{" + actionSelector + ".entitytype}}");
        attrs.put("enabledStates", "{{" + actionSelector + ".enabledStates}}");
        attrs.put("shortDesc", "{{" + actionSelector + ".shortDesc}}");
        attrs.put("longDesc", "{{" + actionSelector + ".longDesc}}");
        attrs.put("currentState", "{{currentState}}");

        return attrs;
    }
}
