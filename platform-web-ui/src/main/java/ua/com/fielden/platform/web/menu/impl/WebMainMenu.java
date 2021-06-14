package ua.com.fielden.platform.web.menu.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import ua.com.fielden.platform.menu.Module;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
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
        final AtomicInteger actionNumber = new AtomicInteger(0);
        return modules.stream().map(module -> {
            final Module moduleEntity = module.getModule(actionNumber.get());
            actionNumber.addAndGet(module.getActions().size());
            return moduleEntity;
        }).collect(Collectors.toList());
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
