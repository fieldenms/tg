package ua.com.fielden.platform.web.view.master.api.actions.entity.impl;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.join;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresentForClass;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.web.centre.WebApiUtils.webComponent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
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
    private final String postActionFunction;
    private final String postActionErrorFunction;

    /**
     * Creates {@link DefaultEntityAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public DefaultEntityAction(final String name, final Class<T> entityType, final String postActionFunction, final String postActionErrorFunction) {
        super(name, webComponent("master/actions/tg-action"));
        this.postActionFunction = postActionFunction;
        this.postActionErrorFunction = postActionErrorFunction;
        if (isActionWithOptions(name, entityType)) {
            this.setExcludeClose(false);
            if (!isNewRestricted(entityType)) {
                this.setExcludeNew(false);
            }
        }
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();

        if ("save".equals(this.name().toLowerCase())) {
            attrs.put("id", "_saveAction");
        }

        attrs.put("exclude-new", this.isExcludeNew());
        attrs.put("exclude-close", this.isExcludeClose());

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

    private static <T extends AbstractEntity<?>> boolean isNewRestricted(final Class<T> entityType) {
        return isAnnotationPresentForClass(RestrictCreationByUsers.class, entityType)|| AbstractEntity.class.isAssignableFrom(AnnotationReflector.getKeyType(entityType));
    }

    private static <T extends AbstractEntity<?>> boolean isActionWithOptions (final String name, final Class<T> entityType) {
        return ("save".equals(name.toLowerCase()) || "refresh".equals(name.toLowerCase())) && isPersistedEntityType(entityType) && !AbstractFunctionalEntityWithCentreContext.class.isAssignableFrom(entityType);
    }

    private String postActionFunction() {
        return postActionFunction;
    }

    private String postActionErrorFunction() {
        return postActionErrorFunction;
    }

    private String augmentShortcutWith(final String shortcut, final String key) {
        final List<List<String>> shortcuts = listOf(shortcut.split(" ")).stream().map(i -> listOf(i.split("\\+"))).collect(toList());
        return shortcuts.stream().map(i -> {
            i.add(1, key);
            return join(i, "+");
        }).collect(joining(" "));
    }

    @Override
    public String shortcut() {
        final String shortcut = super.shortcut();
        final List<String> shortcuts = new ArrayList<>();
        shortcuts.add(shortcut);
        if (!this.isExcludeClose()) {
            shortcuts.add(augmentShortcutWith(shortcut, "shift"));
        }
        if (!this.isExcludeNew()) {
            shortcuts.add(augmentShortcutWith(shortcut, "alt"));
        }
        return join(shortcuts, " ");
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
