package ua.com.fielden.platform.web.menu.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.menu.Module;
import ua.com.fielden.platform.utils.Pair;
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

    public Pair<DomElement, JsCode> generateMenuActions() {
        int numberOfActions = 0;
        final List<DomElement> actionDomElements = new ArrayList<>();
        final List<String> propActions = new ArrayList<>();
        for (final WebMenuModule webMenuModule : modules) {
            for (final EntityActionConfig config : webMenuModule.getActions()) {
                final FunctionalActionElement actionElement = new FunctionalActionElement(config, numberOfActions++, FunctionalActionKind.TOP_LEVEL);
                actionDomElements.add(actionElement.render().attr("slot", webMenuModule.title));
                propActions.add(actionElement.createActionObject());
            }
        }
        return new Pair<>(new DomContainer().add(actionDomElements.toArray(new DomElement[0])), new JsCode(StringUtils.join(propActions, ",\n")));
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
