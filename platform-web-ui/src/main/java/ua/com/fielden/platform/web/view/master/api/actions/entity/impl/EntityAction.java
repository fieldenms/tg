package ua.com.fielden.platform.web.view.master.api.actions.entity.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.IExecutable;
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
public class EntityAction extends AbstractAction implements IRenderable, IExecutable {
    /**
     * Creates {@link EntityAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public EntityAction(final String name, final String actionComponentPath, final Class<? extends AbstractEntity<?>> functionalEntityType, final IPreAction preAction, final IPostAction postActionSuccess, final IPostAction postActionError, final EnabledState enabledState, final String icon, final String shortDesc, final String longDesc) {
        super(name, actionComponentPath, functionalEntityType, preAction, postActionSuccess, postActionError, enabledState, icon, shortDesc, longDesc);
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
        attrs.put("preAction", "{{" + actionSelector + ".preAction}}");
        attrs.put("postActionSuccess", "{{" + actionSelector + ".postActionSuccess}}");
        attrs.put("postActionError", "{{" + actionSelector + ".postActionError}}");
        attrs.put("enabledStates", "{{" + actionSelector + ".enabledStates}}");
        attrs.put("shortDesc", "{{" + actionSelector + ".shortDesc}}");
        attrs.put("longDesc", "{{" + actionSelector + ".longDesc}}");
        attrs.put("currentState", "{{currentState}}");

        return attrs;
    }

    @Override
    public final DomElement render() {
        return new DomElement(this.actionComponentName()).attrs(createAttributes());
    }
}
