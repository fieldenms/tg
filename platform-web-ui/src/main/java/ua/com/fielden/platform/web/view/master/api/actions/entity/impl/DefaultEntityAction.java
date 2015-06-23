package ua.com.fielden.platform.web.view.master.api.actions.entity.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.impl.AbstractAction;

/**
 * The implementation box for default entity actions (like 'Refresh', 'Edit' etc.).
 *
 * @author TG Team
 *
 */
public class DefaultEntityAction extends AbstractAction implements IRenderable, IExecutable {
    private final String onActionFunction;
    private final String onActionErrorFunction;

    /**
     * Creates {@link DefaultEntityAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public DefaultEntityAction(final String name, final String onActionFunction, final String onActionErrorFunction) {
        super(name, "master/actions/tg-action");
        this.onActionFunction = onActionFunction;
        this.onActionErrorFunction = onActionErrorFunction;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();

        final String actionSelector = "_actions['" + this.name() + "']";

        attrs.put("action", "[[" + actionSelector + ".action]]");
        attrs.put("TODO on-action", "[[" + this.onActionFunction() + "]]");
        attrs.put("TODO on-action-error", "[[" + this.onActionErrorFunction() + "]]");

        return attrs;
    }

    private String onActionFunction() {
        return onActionFunction;
    }

    private String onActionErrorFunction() {
        return onActionErrorFunction;
    }

    @Override
    public DomElement render() {
        return new DomElement(this.actionComponentName()).attrs(createAttributes()).attrs(createCustomAttributes());
    }

    @Override
    public JsCode code() {
        final String code =
                wrap1("self._actions['" + name() + "'].shortDesc = '%s';", shortDesc()) + //
                wrap1("self._actions['" + name() + "'].longDesc = '%s';", longDesc()) + //
                wrap1("self._actions['" + name() + "'].icon = '%s';", icon());
        return new JsCode(code);
    }
}
