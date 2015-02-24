package ua.com.fielden.platform.web.view.master.api.actions.entity.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;
import ua.com.fielden.platform.web.view.master.api.actions.impl.AbstractAction;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * The implementation box for entity actions.
 *
 * @author TG Team
 *
 */
public abstract class AbstractEntityAction extends AbstractAction implements IRenderable {
    /**
     * Creates {@link AbstractEntityAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public AbstractEntityAction(final String name, final Class<? extends AbstractEntity<?>> functionalEntityType, final IPreAction preAction, final IPostAction postActionSuccess, final IPostAction postActionError, final EnabledState enabledState, final String icon, final String shortDesc, final String longDesc) {
        super(name, "master/actions/tg-entity-action.html", functionalEntityType, preAction, postActionSuccess, postActionError, enabledState, icon, shortDesc, longDesc);
    }

    /**
     * Creates an attributes that will be used for entity action component generation.
     *
     * @return
     */
    private Map<String, Object> createAttributes() {
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

    /**
     * Creates an attributes that will be used for entity action component generation.
     * <p>
     * Please, implement this method in descendants (for concrete entity actions) to extend the attributes set by action-specific attributes.
     *
     * @return
     */
    protected abstract Map<String, Object> createCustomAttributes();

    @Override
    public final DomElement render() {
        return new DomElement(this.actionComponentName()).attrs(createAttributes()).attrs(createCustomAttributes());
    }
}
