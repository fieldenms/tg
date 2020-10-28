package ua.com.fielden.platform.web.menu.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ua.com.fielden.platform.menu.Action;
import ua.com.fielden.platform.menu.Module;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.menu.module.impl.WebMenuModule;
import ua.com.fielden.platform.web.minijs.JsCode;

public class WebMainMenu {

    private final List<WebMenuModule> modules = new ArrayList<>();

    public WebMenuModule addModule(final String title) {
        final WebMenuModule module = new WebMenuModule(title);
        modules.add(module);
        return module;
    }

    public List<Module> getModules() {
        return modules.stream().map(module -> module.getModule()).collect(Collectors.toList());
    }

    public List<Action> generateMenuActions() {
        int numberOfActions = 0;
        final List<Action> actions = new ArrayList<>();
        for (final WebMenuModule webMenuModule : modules) {
            for (final EntityActionConfig config : webMenuModule.getActions()) {
                final FunctionalActionElement actionElement = new FunctionalActionElement(config, numberOfActions++, FunctionalActionKind.TOP_LEVEL);
                final Map<String, Object> attributes = actionElement.createAttributes();
                final Action action = new Action();
                action.setKey(attributes.get("element-name").toString());
                action.setDesc(attributes.get("short-desc").toString());
                action.setUiRole(uiRole)
                action.setLongDesc(longDesc)
                action.setShortcut(shortcut)
                action.setIcon(icon)
                action.setIconStyle(iconStyle)
                action.setRefreshParentCentreAfterSave(refreshParentCentreAfterSave)
                action.setComponentUri(componentUri)
                action.setDynamicAction(dynamicAction)
                action.setNumberOfAction(numberOfAction)
                action.setActionKind(actionKind)
                action.setElementAlias(elementAlias)
                action.setChosenProperty(chosenProperty)
                action.setSelectionCriteriaRequired(selectionCriteriaRequired)
                action.setRequireSelectedEntities(requireSelectedEntities)
                action.setMasterEntityRequired(masterEntityRequired)
                action.setModuleName(webMenuModule.title);
                action.setPreAction(actionElement.createPreAction());
                action.setPostActionSuccess(actionElement.createPostActionSuccess());
                action.setPostActionError(actionElement.createPostActionError());
                action.setAttrs(actionElement.createElementAttributes());
                actions.add(action);
            }
        }
        return actions;
    }

    public EntityActionConfig getActionConfig(final int actionNumber, final FunctionalActionKind actionKind) {
        if (actionKind == FunctionalActionKind.TOP_LEVEL) {
            int numberOfActions = 0;
            for (final WebMenuModule webMenuModule : modules) {
                for (final EntityActionConfig config : webMenuModule.getActions()) {
                    if (numberOfActions == actionNumber) {
                        return config;
                    }
                    numberOfActions++;
                }
            }
        }
        return null;
    }

    JsCode createActionsObject() {
        return new JsCode(null);
    }
}
