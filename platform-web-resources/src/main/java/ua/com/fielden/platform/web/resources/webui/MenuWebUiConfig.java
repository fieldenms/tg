package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Injector;

import ua.com.fielden.platform.menu.Menu;
import ua.com.fielden.platform.menu.MenuProducer;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.menu.impl.MainMenuBuilder;
import ua.com.fielden.platform.web.view.master.EntityMaster;

public class MenuWebUiConfig {

    public final EntityMaster<Menu> master;

    public MenuWebUiConfig(final Injector injector, final MainMenuBuilder menuBuilder) {
        master = createMaster(injector, menuBuilder);
    }

    private static EntityMaster<Menu> createMaster(final Injector injector, final MainMenuBuilder menuBuilder) {
        return new EntityMaster<Menu>(Menu.class, MenuProducer.class, null, injector) {
            @Override
            public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
                return menuBuilder.getActionConfig(actionNumber, actionKind);
            }
        };
    }

}
