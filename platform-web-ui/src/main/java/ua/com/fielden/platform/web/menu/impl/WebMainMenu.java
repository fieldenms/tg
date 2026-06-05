package ua.com.fielden.platform.web.menu.impl;

import ua.com.fielden.platform.menu.ModuleMenu;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.menu.module.impl.WebMenuModule;
import ua.com.fielden.platform.web.minijs.JsCode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class WebMainMenu {

    private final List<WebMenuModule> modules = new ArrayList<>();

    public WebMenuModule addModule(final String title) {
        final WebMenuModule module = new WebMenuModule(title);
        modules.add(module);
        return module;
    }

    /**
     * Iterates over all web menu module definitions and builds them, returning a list of modules.
     *
     * @return
     */
    public List<ModuleMenu> buildModules() {
        // all tile actions must have a unique index across all modules
        // hence the use of the same atomic integer instance to supply sequential indexes for all tile actions
        final AtomicInteger seqIndex = new AtomicInteger(0);
        return modules.stream().map(module -> module.buildModule(() -> seqIndex.getAndIncrement())).collect(toList());
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

    public Stream<EntityActionConfig> streamActionConfigs() {
        return modules.stream().map(WebMenuModule::getActions).flatMap(List::stream);
    }

}
