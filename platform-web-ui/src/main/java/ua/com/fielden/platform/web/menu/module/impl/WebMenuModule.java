package ua.com.fielden.platform.web.menu.module.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import ua.com.fielden.platform.menu.Action;
import ua.com.fielden.platform.menu.ModuleMenu;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;

public class WebMenuModule {

    public final String title;

    private String description;
    private String bgColor;
    private String captionBgColor;
    private String icon;
    private String detailIcon;
    private WebMenu menu;
    private WebView view;
    private List<EntityActionConfig> actions = new ArrayList<>();

    public WebMenuModule(final String title) {
        this.title = title;
    }

    public WebMenuModule description(final String description) {
        this.description = description;
        return this;
    }

    public WebMenuModule bgColor(final String bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public WebMenuModule captionBgColor(final String captionBgColor) {
        this.captionBgColor = captionBgColor;
        return this;
    }

    public WebMenuModule icon(final String icon) {
        this.icon = icon;
        return this;
    }

    public WebMenuModule detailIcon(final String detailIcon) {
        this.detailIcon = detailIcon;
        return this;
    }

    public WebMenu menu() {
        this.menu = new WebMenu();
        return this.menu;
    }

    public WebMenuModule view(final WebView view) {
        this.view = view;
        return this;
    }

    public WebMenuModule addAction(final EntityActionConfig action) {
        this.actions.add(action);
        return this;
    }

    public List<EntityActionConfig> getActions() {
        return actions;
    }

    /**
     * Builds a web menu module, including its tile actions.
     *
     * @param tileActionIndexSupplier – a supplier of sequential indexes for tile actions.
     * @return
     */
    public ModuleMenu buildModule(final Supplier<Integer> tileActionIndexSupplier) {
        final ModuleMenu module = new ModuleMenu().
                setBgColor(bgColor).
                setCaptionBgColor(captionBgColor).
                setIcon(icon).
                setDetailIcon(detailIcon).
                setKey(title).
                setDesc(description).
                setActions(buildTileActions(tileActionIndexSupplier));
        //TODO module menu can not be null. Right now platform supports modules with view. This case should be covered with separate issue.
        if (this.menu != null) {
            module.setMenu(menu.getMenu());
        } else if (view != null) {
            module.setView(view.getView());
        }
        return module;
    }

    /**
     * Builds tile actions for this web menu module.
     *
     * @param tileActionIndexSupplier – a supplier of unique sequential indexes for tile actions.
     * @return
     */
    private List<Action> buildTileActions(final Supplier<Integer> tileActionIndexSupplier) {
        final List<Action> actions = new ArrayList<>();
        for (final EntityActionConfig config : getActions()) {
            final FunctionalActionElement actionElement = new FunctionalActionElement(config, tileActionIndexSupplier.get(), FunctionalActionKind.TOP_LEVEL);
            final Map<String, Object> attributes = actionElement.createAttributes();
            final Action action = new Action();
            action.setKey(attributes.get("element-name").toString());
            action.setDesc(attributes.get("short-desc").toString());
            action.setUiRole(attributes.get("ui-role").toString());
            action.setLongDesc(attributes.get("long-desc").toString());
            action.setShortcut(attributes.getOrDefault("shortcut", "").toString());
            action.setIcon(attributes.get("icon").toString());
            action.setIconStyle(attributes.get("icon-style").toString());
            action.setRefreshParentCentreAfterSave((Boolean)attributes.get("should-refresh-parent-centre-after-save"));
            action.setComponentUri(attributes.get("component-uri").toString());
            action.setDynamicAction((Boolean)attributes.getOrDefault("dynamic-action", false));
            action.setNumberOfAction((Integer)attributes.get("number-of-action"));
            action.setActionKind(attributes.get("action-kind").toString());
            action.setElementAlias(attributes.getOrDefault("element-alias", "").toString());
            action.setChosenProperty(attributes.getOrDefault("chosen-property", "").toString());
            action.setRequireSelectionCriteria(attributes.get("require-selection-criteria").toString());
            action.setRequireSelectedEntities(attributes.get("require-selected-entities").toString());
            action.setRequireMasterEntity(attributes.get("require-master-entity").toString());
            action.setModuleName(title);
            action.setPreAction(actionElement.createPreAction());
            action.setPostActionSuccess(actionElement.createPostActionSuccess());
            action.setPostActionError(actionElement.createPostActionError());
            action.setAttrs(actionElement.createElementAttributes(true));
            actions.add(action);
        }
        return actions;
    }

}