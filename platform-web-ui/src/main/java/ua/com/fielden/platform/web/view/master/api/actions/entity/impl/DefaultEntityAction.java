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
    private final String postActionFunction;
    private final String postActionErrorFunction;

    /**
     * Creates {@link DefaultEntityAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public DefaultEntityAction(final String name, final String postActionFunction, final String postActionErrorFunction) {
        super(name, "master/actions/tg-action");
        this.postActionFunction = postActionFunction;
        this.postActionErrorFunction = postActionErrorFunction;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();

        final String actionSelector = "_actions['" + this.name() + "']";

        attrs.put("action", "[[" + actionSelector + ".action]]");
        attrs.put("post-action", "[[" + this.postActionFunction() + "]]");
        attrs.put("post-action-error", "[[" + this.postActionErrorFunction() + "]]");

        return attrs;
    }

    private String postActionFunction() {
        return postActionFunction;
    }

    private String postActionErrorFunction() {
        return postActionErrorFunction;
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
