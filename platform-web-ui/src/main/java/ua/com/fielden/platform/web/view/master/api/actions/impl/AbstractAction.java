package ua.com.fielden.platform.web.view.master.api.actions.impl;

import static java.lang.String.format;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;

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
    private String shortcut;
    private final String actionComponentName;
    private final String actionComponentPath;
    private final String indent = "                    ";

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
    
    public void setShortcut(final String shortcut) {
        this.shortcut = shortcut;
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
    
    public String shortcut() {
        return shortcut;
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

        final String actionSelector = "_actions." + this.name();
        attrs.put("entity-type", "[[" + actionSelector + ".entityType]]");
        attrs.put("enabled-states", "[[" + actionSelector + ".enabledStates]]");
        attrs.put("short-desc", "[[" + actionSelector + ".shortDesc]]");
        attrs.put("long-desc", "[[" + actionSelector + ".longDesc]]");
        attrs.put("current-state", "[[currentState]]");
        
        if (shortcut != null) {
            attrs.put("shortcut", shortcut);
        }

        return attrs;
    }

    @Override
    public String importPath() {
        return actionComponentPath;
    }

    protected String wrap0(final String code) {
        return indent + format(code) + "\n";
    }

    protected String wrap0(final String code, final Object smth) {
        return wrap0(code, smth, () -> smth.toString());
    }

    /**
     * Wraps the code into indentation and line-ending symbols.
     *
     * <code>smth</code> is required parameter. If <code>null</code> -- {@link NullPointerException} will throw.
     *
     * @param code
     * @param smth
     * @param f
     * @return
     */
    protected String wrap0(final String code, final Object smth, final Supplier<String> f) {
        if (smth == null) {
            throw new NullPointerException();
        }
        return wrap_1(code, f.get());
    }

    protected String wrap1(final String code, final Object smth) {
        return wrap1(code, smth, () -> smth.toString());
    }

    /**
     * Wraps the code into indentation and line-ending symbols.
     *
     * <code>smth</code> is the optional parameter. If <code>null</code> -- all line will be disregarded.
     *
     * @param code
     * @param smth
     * @param f
     * @return
     */
    protected String wrap1(final String code, final Object smth, final Supplier<String> f) {
        if (smth == null) {
            return "";
        }
        return wrap_1(code, f.get());
    }

    private String wrap_1(final String code, final String insertionCode) {
        return StringUtils.isEmpty(insertionCode) ? "" : indent + format(code, insertionCode) + "\n";
    }

    protected String indent() {
        return indent;
    }
}
