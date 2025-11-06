package ua.com.fielden.platform.web.centre.api.resultset.impl;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.impl.AbstractCriterionWidget;
import ua.com.fielden.platform.web.interfaces.IImportable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.actions.IAction;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * The implementation for functional entity actions (DOM element).
 *
 * @author TG Team
 *
 */
public class FunctionalActionElement implements IRenderable, IImportable {
    private final String widgetName;
    private final String widgetPath;
    private boolean debug = false;
    public final EntityActionConfig entityActionConfig;
    public final int numberOfAction;
    private final FunctionalActionKind functionalActionKind;
    private final String chosenProperty;
    /** Should be <code>true</code> in case where functional action element is inside entity master, otherwise it is inside entity centre. */
    private boolean forMaster = false;

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
     * Creates an entity (aka primary) action for master.
     *
     * @param entityActionConfig
     * @param numberOfAction
     * @return
     */
    public static FunctionalActionElement newEntityActionForMaster(final EntityActionConfig entityActionConfig, final int numberOfAction) {
        final FunctionalActionElement el = new FunctionalActionElement(entityActionConfig, numberOfAction, FunctionalActionKind.PRIMARY_RESULT_SET);
        el.setForMaster(true);
        return el;
    }

    /**
     * Creates a property action for master.
     *
     * @param entityActionConfig
     * @param numberOfAction
     * @return
     */
    public static FunctionalActionElement newPropertyActionForMaster(final EntityActionConfig entityActionConfig, final int numberOfAction, final String propName) {
        final FunctionalActionElement el = new FunctionalActionElement(entityActionConfig, numberOfAction, FunctionalActionKind.PRIMARY_RESULT_SET, propName);
        el.setForMaster(true);
        return el;
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

        this.chosenProperty = chosenProperty;
    }

    /**
     * Creates attributes that are used for the widget component generation (generic attributes).
     *
     * @return
     */
    public Map<String, Object> createAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();
        if (isDebug()) {
            attrs.put("debug", "true");
        }

        if (FunctionalActionKind.TOP_LEVEL == functionalActionKind) {
            attrs.put("class", "entity-specific-action");
            attrs.put("slot", "entity-specific-action");
        } else if (FunctionalActionKind.MENU_ITEM == functionalActionKind) {
            attrs.put("slot", "menu-item-action");
            attrs.put("data-route", getDataRoute());
        } else if (FunctionalActionKind.FRONT == functionalActionKind) {
            attrs.put("slot", "custom-front-action");
        } else if (FunctionalActionKind.SHARE == functionalActionKind) {
            attrs.put("share-action", "true");
            attrs.put("slot", "custom-share-action");
            attrs.put("hidden", "[[embedded]]"); // let's completely hide the share action for embedded centres
            attrs.put("disabled", "[[_shareButtonDisabled]]");
            attrs.put(" style", "[[_computeButtonStyle(_shareButtonDisabled)]]"); // included a space before "style" due to restriction in DOM API that requires only value pairs with ':' separator
        }

        attrs.put("ui-role", conf().role.toString());
        attrs.put("short-desc", getShortDesc());
        attrs.put("long-desc", conf().longDesc.isPresent() ? conf().longDesc.get() : "");
        if (conf().shortcut.isPresent()) {
            attrs.put("shortcut", conf().shortcut.get());
        }
        attrs.put("icon", getIcon());
        attrs.put("icon-style", getIconStyle());
        attrs.put("should-refresh-parent-centre-after-save", conf().shouldRefreshParentCentreAfterSave);
        attrs.put("component-uri", generateComponentUri());
        final String elementName = generateElementName();
        attrs.put("element-name", elementName);
        //If action has no functional entity type and it's noAction property is false then
        //it is dynamic action that determines it's element name and import uri by current entity and if exists chosen property.
        if (!conf().functionalEntity.isPresent()) {
            attrs.put("dynamic-action", true);
        }
        attrs.put("number-of-action", numberOfAction);
        attrs.put("action-kind", functionalActionKind);
        conf().functionalEntity.ifPresent(funcType ->  {
            attrs.put("element-alias", elementName + "_" + numberOfAction + "_" + functionalActionKind);
        });

        // in case of an menu item action show-dialog assignment happens within tg-master-menu
        if (FunctionalActionKind.INSERTION_POINT == functionalActionKind) {
            attrs.put("show-dialog", "[[_showInsertionPoint]]");
        } else if (FunctionalActionKind.MENU_ITEM != functionalActionKind) {
            attrs.put("show-dialog", "[[_showDialog]]");
        }
        //Specify taoster to show error message via the toast
        attrs.put("toaster", "[[toaster]]");

        // in case of an action that models a menu item for an entity master with menu, context gets assigned
        // only after the main entity is saved at the client side as part of tg-master-menu logic.
        if (FunctionalActionKind.MENU_ITEM != functionalActionKind) {
            attrs.put("create-context-holder", "[[_createContextHolder]]");
        }

        final String actionsHolderName = functionalActionKind.holderName;
        attrs.put("attrs", "[[" + actionsHolderName + "." + numberOfAction + ".attrs]]");
        attrs.put("pre-action", "[[" + actionsHolderName + "." + numberOfAction + ".preAction]]");
        attrs.put("post-action-success", "[[" + actionsHolderName + "." + numberOfAction + ".postActionSuccess]]");
        attrs.put("post-action-error", "[[" + actionsHolderName + "." + numberOfAction + ".postActionError]]");

        // chosenProperty should be ignored strictly when it is null as an empty value means 'this'
        if (chosenProperty != null) {
            attrs.put("chosen-property", chosenProperty);
        }

        if (conf().context.isPresent()) {
            attrs.put("require-selection-criteria", conf().context.get().withSelectionCrit ? "true" : "false");
            attrs.put("require-selected-entities", conf().context.get().withCurrentEtity ? "ONE" : (conf().context.get().withAllSelectedEntities ? "ALL" : "NONE"));
            attrs.put("require-master-entity", conf().context.get().withMasterEntity ? "true" : "false");
            if (!conf().context.get().relatedContexts.isEmpty()) {
                attrs.put("related-contexts", "[[" + actionsHolderName + "." + numberOfAction + ".relatedContexts]]");
            }
            conf().context.get().parentCentreContext.ifPresent(parentCentreContext -> {
                attrs.put("parent-centre-context", "[[" + actionsHolderName + "." + numberOfAction + ".parentCentreContext]]");
            });
        } else {
            attrs.put("require-selection-criteria", "null");
            attrs.put("require-selected-entities", "null");
            attrs.put("require-master-entity", "null");
        }

        conf().actionIdentifier.ifPresent(actionIdentifier -> attrs.put("action-id", actionIdentifier));

        return attrs;
    }

    /**
     * Generates element name for corresponding functional Entity Master.
     */
    public String generateElementName() {
        return conf().functionalEntity.map(entityType -> "tg-" + entityType.getSimpleName() + "-master").orElse("");
    }

    private String generateComponentUri() {
        return conf().functionalEntity.map(entityType -> "/master_ui/" + entityType.getName()).orElse("");
    }

    public String getDataRoute() {
        return conf().functionalEntity.map(entityType -> entityType.getSimpleName()).orElse("");
    }

    public String getIcon() {
        return conf().icon.isPresent() ? conf().icon.get() : "editor:mode-edit";
    }

    public String getIconStyle() {
        return conf().iconStyle.orElse("");
    }

    public String getShortDesc() {
        return conf().shortDesc.isPresent() ? conf().shortDesc.get() : "";
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
    }

    public EntityActionConfig conf() {
        return entityActionConfig;
    }

    @Override
    public final DomElement render() {
        return new DomElement(widgetName).attrs(createAttributes()).attrs(createCustomAttributes());
    }

    @Override
    public String importPath() {
        return widgetPath;
    }

    public FunctionalActionKind getFunctionalActionKind() {
        return functionalActionKind;
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
        final StringBuilder attrs = new StringBuilder("{\n");
        if (conf().context.isPresent()) {
            if (!conf().context.get().relatedContexts.isEmpty()) {
                attrs.append("relatedContexts: ").append(createRelatedContexts(conf().context.get().relatedContexts)).append(",\n");
            }
            conf().context.get().parentCentreContext.ifPresent(parentCentreContext -> {
                attrs.append("parentCentreContext: ").append(createParentCentreContext(parentCentreContext)).append(",\n");
            });
        }
        attrs.append("preAction: ").append(createPreAction()).append(",\n");
        attrs.append("postActionSuccess: ").append(createPostActionSuccess()).append(",\n");
        attrs.append("attrs: ").append(createElementAttributes(false)).append(",\n");
        attrs.append("postActionError: ").append(createPostActionError()).append("\n");
        return attrs.append("}\n").toString();
    }

    private String createParentCentreContext(final CentreContextConfig context) {
        final StringBuilder attrs = new StringBuilder("{\n");
        attrs.append(createContextAttributes(context));
        return attrs.append("}").toString();
    }

    private String createRelatedContexts(final Map<Class<? extends AbstractFunctionalEntityWithCentreContext<?>>, CentreContextConfig> relatedContexts) {
        final StringBuilder relatedContextsList = new StringBuilder("[");
        relatedContextsList.append(relatedContexts.entrySet().stream().map(relatedContext -> createRelatedContext(relatedContext.getKey(), relatedContext.getValue())).collect(joining(",")));
        return relatedContextsList.append("]").toString();
    }

    private String createRelatedContext(final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> funcType, final CentreContextConfig context) {
        final StringBuilder attrs = new StringBuilder("{\n");
        attrs.append("elementName: ").append("'" + format("tg-%s-master", funcType.getSimpleName()) + "'").append(",\n");
        attrs.append(createContextAttributes(context));
        return attrs.append("}").toString();
    }

    private String createContextAttributes(final CentreContextConfig context) {
        final StringBuilder attrs = new StringBuilder("");
        if (!context.relatedContexts.isEmpty()) {
            attrs.append("relatedContexts: ").append(createRelatedContexts(context.relatedContexts)).append(",\n");
        }
        context.parentCentreContext.ifPresent(parentContext -> {
            attrs.append("parentCentreContext: ").append(createParentCentreContext(parentContext)).append(",\n");
        });
        attrs.append("requireSelectionCriteria: ").append(context.withSelectionCrit ? "'true'" : "'false'").append(",\n");
        attrs.append("requireSelectedEntities: ").append(context.withCurrentEtity ? "'ONE'" : (context.withAllSelectedEntities ? "'ALL'" : "'NONE'")).append(",\n");
        attrs.append("requireMasterEntity: ").append(context.withMasterEntity ? "'true'" : "'false'").append("\n");

        return attrs.toString();
    }

    private String createExcludeInsertionPoints() {
        return "[" + join(conf().excludeInsertionPoints.stream().map(insertionPointType -> "'tg-" + insertionPointType.getSimpleName() + "-master'").collect(toList()), ",") + "]";
    }

    /**
     * Creates non-empty JS function string for {@code bodyOpt}. Generated function logs the {@code name} and {@code actionShortDescOpt} and executes the body.
     *
     * @param params -- string of parameters for the generated function
     * @param codeOptIfEmptyBody -- code to be executed if body is empty
     * @return
     */
    private static String createFunctionBody(final Optional<? extends IAction> bodyOpt, final String name, final String params, final Optional<String> codeOptIfEmptyBody, final Optional<String> actionShortDescOpt) {
        final StringBuilder code = new StringBuilder();
        code.append(format("function (%s) {\n", params));
        code.append(format("    console.log('%s: %s');\n", name, actionShortDescOpt.orElse("noname")));
        if (bodyOpt.isPresent()) {
            code.append(bodyOpt.get().build().toString());
        } else {
            codeOptIfEmptyBody.ifPresent((c) -> code.append(c));
        }
        code.append("}");
        return code.toString();
    }

    /**
     * Creates JS function for {@link IPreAction}.
     */
    public String createPreAction() {
        return createFunctionBody(conf().preAction, "preAction", "action", of("    return Promise.resolve(true);\n"), conf().shortDesc);
    }

    /**
     * Creates JS function for {@link IPostAction}.
     */
    public String createPostActionFunctionBody(final Optional<? extends IAction> actionFunction, final String name) {
        return createFunctionBody(actionFunction, name, "functionalEntity, action, master", empty(), conf().shortDesc);
    }

    /**
     * Creates JS function for successful {@link IPostAction}.
     */
    public String createPostActionSuccess() {
        return createPostActionFunctionBody(conf().successPostAction, "postActionSuccess");
    }

    /**
     * Creates JS function for erroneous {@link IPostAction}.
     */
    public String createPostActionError() {
        return createPostActionFunctionBody(conf().errorPostAction, "postActionError");
    }

    /**
     * Creates action 'attrs' for generation ({@code asString} === false) or for client-side parsing in 'tg-app-template.postRetrieved' method ({@code asString} === true).
     *
     * @param asString
     * @return
     */
    public String createElementAttributes(final boolean asString) {
        final StringBuilder code = new StringBuilder();
        final String keyQ = asString ? "\"" : "";
        final String valueQ = asString ? "\"" : "'";
        code.append("{\n");
        conf().functionalEntity.ifPresent(entityType -> {
            code.append("    " + keyQ + "entityType" + keyQ + ": " + valueQ + entityType.getName() + valueQ + ",\n");
        });
        if (!conf().excludeInsertionPoints.isEmpty()) {
            code.append("    " + keyQ +"excludeInsertionPoints" + keyQ + ": " + keyQ + createExcludeInsertionPoints() + keyQ + ",\n");
        }
        code.append("    " + keyQ + "currentState" + keyQ + ": " + valueQ + "EDIT" + valueQ + ",\n");
        code.append("    " + keyQ + "centreUuid" + keyQ + ": " + keyQ + "self.uuid" + keyQ); // value surrounded with "" -- will be interpreted in tg-app-template specifically

        conf().prefDimForView.ifPresent(prefDim -> {
            code.append(format(",\n    " +
                keyQ + "prefDim" + keyQ + ": " + "{" +
                    keyQ + "width" + keyQ + ": " + keyQ + "function() {return %s}" + keyQ +", " + // value surrounded with "" -- will be interpreted in tg-app-template specifically
                    keyQ + "height" + keyQ + ": " + keyQ + "function() {return %s}" + keyQ + ", " + // value surrounded with "" -- will be interpreted in tg-app-template specifically
                    keyQ + "widthUnit" + keyQ + ": " + valueQ + "%s" + valueQ + ", " +
                    keyQ + "heightUnit" + keyQ + ": " + valueQ + "%s" + valueQ +
                "}", prefDim.width, prefDim.height, prefDim.widthUnit.value, prefDim.heightUnit.value
            ));
        });
        code.append("\n}");
        return code.toString();
    }

    public boolean isForMaster() {
        return forMaster;
    }

    public void setForMaster(final boolean forMaster) {
        this.forMaster = forMaster;
    }
}
