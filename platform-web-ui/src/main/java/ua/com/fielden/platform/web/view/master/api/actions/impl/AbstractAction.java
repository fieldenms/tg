package ua.com.fielden.platform.web.view.master.api.actions.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.web.interfaces.IImportable;
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
public abstract class AbstractAction implements IImportable {
    private final String name;
    private EnabledState enabledState;
    private String icon;
    private String shortDesc;
    private String longDesc;
    private final String actionComponentName;
    private final String actionComponentPath;

    /**
     * Creates {@link AbstractAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public AbstractAction(final String name, final String actionComponentPath) {
        this.name = name;
        this.actionComponentName = AbstractWidget.extractNameFrom(actionComponentPath);
        this.actionComponentPath = actionComponentPath;
    }

    /**
     * The name of the action.
     *
     * @return
     */
    public String name() {
        return name;
    }

    protected String actionComponentName() {
        return actionComponentName;
    }

    protected String actionComponentPath() {
        return actionComponentPath;
    }

    public void setEnabledState(final EnabledState enabledState) {
        this.enabledState = enabledState;
    }

    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public void setShortDesc(final String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public void setLongDesc(final String longDesc) {
        this.longDesc = longDesc;
    }

    public EnabledState enabledState() {
        return enabledState;
    }

    public String icon() {
        return icon;
    }

    public String shortDesc() {
        return shortDesc;
    }

    public String longDesc() {
        return longDesc;
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

    @Override
    public String importPath() {
        return actionComponentPath;
    }
}
