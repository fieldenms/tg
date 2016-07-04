package ua.com.fielden.platform.web.centre.api.resultset.impl;

import static java.lang.String.format;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.PrefDim;
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
     * Creates an attributes that will be used for widget component generation (generic attributes).
     *
     * @return
     */
    private Map<String, Object> createAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();
        if (isDebug()) {
            attrs.put("debug", "true");
        }

        if (FunctionalActionKind.TOP_LEVEL == functionalActionKind) {
            attrs.put("class", "entity-specific-action");
        } else if (FunctionalActionKind.MENU_ITEM == functionalActionKind) {
            attrs.put("class", "menu-item-action");
            attrs.put("data-route", getDataRoute());
        }

        attrs.put("ui-role", conf().role.toString());
        attrs.put("short-desc", getShortDesc());
        attrs.put("long-desc", conf().longDesc.isPresent() ? conf().longDesc.get() : "NOT SPECIFIED");
        if (conf().shortcut.isPresent()) {
            attrs.put("shortcut", conf().shortcut.get());
        }
        attrs.put("icon", getIcon());
        attrs.put("should-refresh-parent-centre-after-save", conf().shouldRefreshParentCentreAfterSave);
        attrs.put("component-uri", "/master_ui/" + conf().functionalEntity.get().getName());
        final String elementName = "tg-" + conf().functionalEntity.get().getSimpleName() + "-master";
        attrs.put("element-name", elementName);
        attrs.put("number-of-action", numberOfAction);
        attrs.put("element-alias", elementName + "_" + numberOfAction + "_" + functionalActionKind);

        // in case of an menu item action show-dialog assignment happens within tg-master-menu
        if (FunctionalActionKind.INSERTION_POINT == functionalActionKind) {
            attrs.put("show-dialog", "[[_showInsertionPoint]]");
        } else if (FunctionalActionKind.MENU_ITEM != functionalActionKind) {
            attrs.put("show-dialog", "[[_showDialog]]");
        }

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
        } else {
            attrs.put("require-selection-criteria", "null");
            attrs.put("require-selected-entities", "null");
            attrs.put("require-master-entity", "null");
        }

        return attrs;
    }

    public String getDataRoute() {
        return conf().functionalEntity.get().getSimpleName();
    }

    public String getIcon() {
        return conf().icon.isPresent() ? conf().icon.get() : "editor:mode-edit";
    }

    public String getShortDesc() {
        return conf().shortDesc.isPresent() ? conf().shortDesc.get() : "NOT SPECIFIED";
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

        attrs.append("preAction: function (action) {\n");
        attrs.append("    console.log('preAction: " + conf().shortDesc.get() + "');\n");
        if (conf().preAction.isPresent()) {
            attrs.append(conf().preAction.get().build().toString());
        } else {
            attrs.append("    return Promise.resolve(true);\n");
        }
        attrs.append("},\n");

        attrs.append("postActionSuccess: function (functionalEntity) {\n");
        attrs.append("    console.log('postActionSuccess: " + conf().shortDesc.get() + "', functionalEntity);\n");
        if (conf().successPostAction.isPresent()) {
            attrs.append(conf().successPostAction.get().build().toString());
        }
        attrs.append("},\n");

        attrs.append("attrs: {\n");
        attrs.append("    entityType:'" + conf().functionalEntity.get().getName() + "', currentState:'EDIT', centreUuid: self.uuid,\n");

        if (conf().prefDimForView.isPresent()) {
            final PrefDim prefDim = conf().prefDimForView.get();
            attrs.append(format("    prefDim: {'width': function() {return %s}, 'height': function() {return %s}, 'widthUnit': '%s', 'heightUnit': '%s'},\n", prefDim.width, prefDim.height, prefDim.widthUnit.value, prefDim.heightUnit.value));
        }

        attrs.append("},\n");

        attrs.append("postActionError: function (functionalEntity) {\n");
        attrs.append("    console.log('postActionError: " + conf().shortDesc.get() + "', functionalEntity);\n");
        if (conf().errorPostAction.isPresent()) {
            attrs.append(conf().errorPostAction.get().build().toString());
        }
        attrs.append("}\n");
        return attrs.append("}\n").toString();
    }

    public boolean isForMaster() {
        return forMaster;
    }

    public void setForMaster(final boolean forMaster) {
        this.forMaster = forMaster;
    }
}
