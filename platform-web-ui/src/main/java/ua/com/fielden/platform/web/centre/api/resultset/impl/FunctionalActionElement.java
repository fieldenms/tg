package ua.com.fielden.platform.web.centre.api.resultset.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.sample.domain.MasterInDialogInvocationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.MasterInvocationFunctionalEntity;
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
    private final FunctionalActionKind functionalActionKind;
    private final boolean masterInvocationAction;
    private final boolean masterInDialogInvocationAction;
    private final String chosenProperty;

    /**
     * Creates {@link FunctionalActionElement} from <code>entityActionConfig</code>.
     *
     * @param entityActionConfig
     */
    public FunctionalActionElement(final EntityActionConfig entityActionConfig, final int numberOfAction, final String chosenProperty) {
        this(entityActionConfig, numberOfAction, FunctionalActionKind.PROP, chosenProperty);
    }

    /**
     * Creates {@link FunctionalActionElement} from <code>entityActionConfig</code>.
     *
     * @param entityActionConfig
     */
    public FunctionalActionElement(final EntityActionConfig entityActionConfig, final int numberOfAction, final FunctionalActionKind functionalActionKind) {
        this(entityActionConfig, numberOfAction, functionalActionKind, null);
    }

    /**
     * Creates {@link FunctionalActionElement} from <code>entityActionConfig</code>.
     *
     * @param entityActionConfig
     */
    private FunctionalActionElement(final EntityActionConfig entityActionConfig, final int numberOfAction, final FunctionalActionKind functionalActionKind, final String chosenProperty) {
        this.widgetName = AbstractCriterionWidget.extractNameFrom("actions/tg-ui-action");
        this.widgetPath = "actions/tg-ui-action";
        this.entityActionConfig = entityActionConfig;
        this.numberOfAction = numberOfAction;
        this.functionalActionKind = functionalActionKind;
        this.masterInvocationAction = this.entityActionConfig.functionalEntity.isPresent()
                && MasterInvocationFunctionalEntity.class.isAssignableFrom(this.entityActionConfig.functionalEntity.get());
        this.masterInDialogInvocationAction = this.entityActionConfig.functionalEntity.isPresent()
                && MasterInDialogInvocationFunctionalEntity.class.isAssignableFrom(this.entityActionConfig.functionalEntity.get());

        this.chosenProperty = chosenProperty;
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

        attrs.put("short-desc", conf().shortDesc.isPresent() ? conf().shortDesc.get() : "NOT SPECIFIED");
        attrs.put("long-desc", conf().longDesc.isPresent() ? conf().longDesc.get() : "NOT SPECIFIED");
        attrs.put("icon", conf().icon.isPresent() ? conf().icon.get() : "editor:mode-edit");
        attrs.put("component-uri", "/master_ui/" + conf().functionalEntity.get().getName());
        attrs.put("show-dialog", "[[_showDialog]]");
        attrs.put("element-name", "tg-" + conf().functionalEntity.get().getSimpleName() + "-master");
        attrs.put("create-context-holder", "[[_createContextHolder]]");
        final String actionsHolderName = functionalActionKind == FunctionalActionKind.TOP_LEVEL ? "topLevelActions" :
                functionalActionKind == FunctionalActionKind.PRIMARY_RESULT_SET ? "primaryAction" :
                        functionalActionKind == FunctionalActionKind.SECONDARY_RESULT_SET ? "secondaryActions" :
                                "propActions";
        attrs.put("attrs", "[[" + actionsHolderName + "." + numberOfAction + ".attrs]]");
        attrs.put("pre-action", "[[" + actionsHolderName + "." + numberOfAction + ".preAction]]");
        attrs.put("post-action", "[[" + actionsHolderName + "." + numberOfAction + ".postActionSuccess]]");
        attrs.put("post-action-error", "[[" + actionsHolderName + "." + numberOfAction + ".postActionError]]");
        if (functionalActionKind == FunctionalActionKind.PROP) {
            attrs.put("chosen-property", chosenProperty);
        }

        if (conf().context.isPresent()) {
            attrs.put("require-selection-criteria", conf().context.get().withSelectionCrit ? "true" : "false");
            attrs.put("require-selected-entities", conf().context.get().withCurrentEtity ? "ONE" : (conf().context.get().withAllSelectedEntities ? "ALL" : "NONE"));
            attrs.put("require-master-entity", conf().context.get().withMasterEntity ? "true" : "false");
        } else {
            attrs.put("require-selection-criteria", "null");
            attrs.put("require-selected-entities", "null");
            attrs.put("require-master-entity", "null");
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
        if (masterInvocationAction) {
            return new DomElement("tg-page-action").attr("class", "primary-action").attr("action", masterInDialogInvocationAction ? "[[_showMasterInDialog]]"
                    : "[[_showMaster]]").attr("short-desc", "action description").attr("icon", "editor:mode-edit");
        } else if (FunctionalActionKind.TOP_LEVEL == functionalActionKind) {
            // final DomElement spanElement = new DomElement("span").attr("class", "span-tooltip").attr("tip", null).add(new InnerTextElement(conf().longDesc.isPresent() ? conf().longDesc.get()
            //         : "Functional Action (NO DESC HAS BEEN SPECIFIED)"));

            // return new DomElement("core-tooltip").attr("class", "delayed entity-specific-action").attr("tabIndex", "-1").add(uiActionElement).add(spanElement);
            return uiActionElement.attr("class", "entity-specific-action");
        } else {
            return uiActionElement;
        }
    }

    @Override
    public String importPath() {
        return masterInvocationAction ? "actions/tg-page-action" : widgetPath;
    }

    public FunctionalActionKind getFunctionalActionKind() {
        return functionalActionKind;
    }

    public boolean isMasterInvocationAction() {
        return masterInvocationAction;
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

        sb.append("attrs: {\n");
        sb.append("    entityType:'" + conf().functionalEntity.get().getName() + "', currentState:'EDIT', centreUuid: self.uuid\n");
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
