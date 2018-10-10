package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;

import com.google.inject.Injector;

import ua.com.fielden.platform.menu.Menu;
import ua.com.fielden.platform.menu.MenuProducer;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.menu.impl.MainMenuBuilder;
import ua.com.fielden.platform.web.view.master.EntityMaster;

public class MenuWebUiConfig {

    public final EntityMaster<Menu> master;

    public MenuWebUiConfig(final Injector injector, final MainMenuBuilder desktopMenuBuilder, final MainMenuBuilder mobileMenuBuilder) {
        master = createMaster(injector, desktopMenuBuilder, mobileMenuBuilder);
    }

    private static EntityMaster<Menu> createMaster(final Injector injector, final MainMenuBuilder desktopMenuBuilder, final MainMenuBuilder mobileMenuBuilder) {
        return new EntityMaster<Menu>(Menu.class, MenuProducer.class, null, injector) {
            
            private final IDeviceProvider deviceProvider = injector.getInstance(IDeviceProvider.class);
            
            @Override
            public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
                final MainMenuBuilder menuBuilder = deviceProvider.getDeviceProfile() == DESKTOP ? desktopMenuBuilder: mobileMenuBuilder;
                return menuBuilder.getActionConfig(actionNumber, actionKind);
            }
        };
    }

}
