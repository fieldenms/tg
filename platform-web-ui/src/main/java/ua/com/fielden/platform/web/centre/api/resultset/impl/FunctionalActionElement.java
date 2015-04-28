package ua.com.fielden.platform.web.centre.api.resultset.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.crit.impl.AbstractCriterionWidget;
import ua.com.fielden.platform.web.interfaces.IImportable;
import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 * The implementation for functional entity actions (dom element).
 *
 * @author TG Team
 *
 */
public class FunctionalActionElement implements IRenderable, IImportable {
    private final String widgetName;
    private final String widgetPath;
    private boolean debug = false;
    private final EntityActionConfig entityActionConfig;
    private final int numberOfAction;

    /**
     * Creates {@link FunctionalActionElement} from <code>entityActionConfig</code>.
     *
     * @param entityActionConfig
     */
    public FunctionalActionElement(final EntityActionConfig entityActionConfig, final int numberOfAction) {
        this.widgetName = AbstractCriterionWidget.extractNameFrom("actions/tg-ui-action");
        this.widgetPath = "actions/tg-ui-action";
        this.entityActionConfig = entityActionConfig;
        this.numberOfAction = numberOfAction;
    }

    /**
     * Creates an attributes that will be used for widget component generation (generic attributes).
     *
     * @return
     */
    private Map<String, Object> createAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();
        if (isDebug()) {
            attrs.put("debug", "true");
        }

        attrs.put("user", "{{user}}");
        attrs.put("shortDesc", conf().shortDesc.isPresent() ? conf().shortDesc.get() : "NOT SPECIFIED");
        attrs.put("longDesc", conf().longDesc.isPresent() ? conf().longDesc.get() : "NOT SPECIFIED");
        attrs.put("icon", conf().icon.isPresent() ? conf().icon.get() : "editor:mode-edit");
        attrs.put("componentUri", "/users/{{user}}/master/" + conf().functionalEntity.get().getName());
        attrs.put("elementName", "tg-" + conf().functionalEntity.get().getSimpleName() + "-master");
        attrs.put("attrs", "{{ {user:user, entitytype:'" + conf().functionalEntity.get().getName() + "', currentState:'EDIT'} }}");
        attrs.put("contextRetriever", "{{createCentreContextHolder}}");
        attrs.put("preAction", "{{topLevelActions[" + numberOfAction + "].preAction}}");
        attrs.put("postActionSuccess", "{{topLevelActions[" + numberOfAction + "].postActionSuccess}}");
        attrs.put("postActionError", "{{topLevelActions[" + numberOfAction + "].postActionError}}");

        if (conf().context.isPresent()) {
            if (conf().context.get().withSelectionCrit) {
                // disregarded at this stage -- sends every time
            }
            attrs.put("requireSelectedEntities", conf().context.get().withCurrentEtity ? "ONE" : (conf().context.get().withAllSelectedEntities ? "ALL" : "NONE"));
            attrs.put("requireMasterEntity", conf().context.get().withMasterEntity ? "true" : "false");
        }

        return attrs;
    }

    /**
     * Creates an attributes that will be used for widget component generation.
     * <p>
     * Please, implement this method in descendants (for concrete widgets) to extend the attributes set by widget-specific attributes.
     *
     * @return
     */
    protected Map<String, Object> createCustomAttributes() {
        return new LinkedHashMap<>();
    };

    public EntityActionConfig conf() {
        return entityActionConfig;
    }

    @Override
    public final DomElement render() {

        final DomElement uiActionElement = new DomElement(widgetName).attrs(createAttributes()).attrs(createCustomAttributes());

        final DomElement spanElement = new DomElement("span").attr("class", "span-tooltip").attr("tip", null).add(new InnerTextElement(conf().longDesc.isPresent() ? conf().longDesc.get() : "Functional Action (NO DESC HAS BEEN SPECIFIED)"));

        return new DomElement("core-tooltip").attr("class", "delayed entity-specific-action").attr("tabIndex", "-1").add(uiActionElement).add(spanElement);
    }

    @Override
    public String importPath() {
        return widgetPath;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    /**
     * Creates a string representation for the object which holds pre- and post-actions.
     *
     * @return
     */
    public String createActionObject() {
        final StringBuilder sb = new StringBuilder("{\n");

        sb.append("preAction: function () {\n");
        sb.append("    console.log('preAction: " + conf().shortDesc.get() + "');\n");
        if (conf().preAction.isPresent()) {
            sb.append(conf().preAction.get().build().toString());
        } else {
            sb.append("    return true;\n");
        }
        sb.append("},\n");

        sb.append("postActionSuccess: function () {\n");
        sb.append("    console.log('postActionSuccess: " + conf().shortDesc.get() + "');\n");
        if (conf().successPostAction.isPresent()) {
            sb.append(conf().successPostAction.get().build().toString());
        }
        sb.append("},\n");

        sb.append("postActionError: function () {\n");
        sb.append("    console.log('postActionError: " + conf().shortDesc.get() + "');\n");
        if (conf().errorPostAction.isPresent()) {
            sb.append(conf().errorPostAction.get().build().toString());
        }
        sb.append("}\n");
        return sb.append("}\n").toString();
    }
}
