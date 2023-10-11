package ua.com.fielden.platform.web.view.master.api.actions.entity.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.RestrictCreationByUsers;
import ua.com.fielden.platform.reflection.AnnotationReflector;
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
public class DefaultEntityAction<T extends AbstractEntity<?>> extends AbstractAction implements IRenderable, IExecutable {
    private final Class<T> entityType;
    private final String postActionFunction;
    private final String postActionErrorFunction;

    /**
     * Creates {@link DefaultEntityAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public DefaultEntityAction(final String name, final Class<T> entityType, final String postActionFunction, final String postActionErrorFunction) {
        super(name, "master/actions/tg-action");
        this.entityType = entityType;
        this.postActionFunction = postActionFunction;
        this.postActionErrorFunction = postActionErrorFunction;
    }

    /*
     * if ((masterAction  == MasterActions.SAVE || masterAction  == MasterActions.REFRESH) && entityType.isAnnotationPresent(MapEntityTo.class) && !AbstractEntity.class.isAssignableFrom(AnnotationReflector.getKeyType(entityType))) {
            final DefaultEntityAction saveAction = new DefaultEntityAction(masterAction.name(), getPostAction(masterAction), getPostActionError(masterAction));
            final MasterActions actionAndClose = MasterActions.valueOf(masterAction.name() + "_AND_CLOSE");
            final DefaultEntityAction andClose = new DefaultEntityAction(actionAndClose.name(), getPostAction(actionAndClose), getPostActionError(actionAndClose));
            if (entityType.isAnnotationPresent(RestrictCreationByUsers.class)) {
                return new EntityActionWithOptions(saveAction, andClose);
            }
            final MasterActions actionAndNew = MasterActions.valueOf(masterAction.name() + "_AND_NEW");
            final DefaultEntityAction andNew = new DefaultEntityAction(actionAndNew.name(), getPostAction(actionAndNew), getPostActionError(actionAndNew));
            return new EntityActionWithOptions(saveAction, andClose, andNew);
        }
     */

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();

        if ("save".equals(this.name().toLowerCase())) {
            attrs.put("id", "_saveAction");
        }
        if (("save".equals(this.name().toLowerCase()) || "refresh".equals(this.name().toLowerCase()))
                && this.entityType.isAnnotationPresent(MapEntityTo.class)
                && !AbstractEntity.class.isAssignableFrom(AnnotationReflector.getKeyType(this.entityType))) {
            attrs.put("action-type", "optionbutton");
            if (entityType.isAnnotationPresent(RestrictCreationByUsers.class)) {
                attrs.put("restrict-new-option", true);
            }
        }
        attrs.put("role", this.name().toLowerCase());
        if (focusingCallback() != null) {
            attrs.put("focusing-callback", "[[" + focusingCallback() + "]]");
        }
        attrs.put("event-channel", "[[centreUuid]]");
        final String actionSelector = "_actions." + this.name();

        attrs.put("action", "[[" + actionSelector + ".action]]");
        attrs.put("new-action", "[[" + actionSelector + ".newAction]]");
        attrs.put("post-action", "{{" + this.postActionFunction() + "}}");
        attrs.put("post-action-error", "{{" + this.postActionErrorFunction() + "}}");

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
                wrap1("self.set('_actions." + name() + ".shortDesc', '%s');", shortDesc()) + //
                wrap1("self.set('_actions." + name() + ".longDesc', '%s');", longDesc()) + //
                wrap1("self.set('_actions." + name() + ".icon', '%s');", icon());
        return new JsCode(code);
    }
}
