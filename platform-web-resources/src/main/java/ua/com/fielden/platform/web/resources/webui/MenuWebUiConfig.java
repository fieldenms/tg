package ua.com.fielden.platform.web.resources.webui;

import ua.com.fielden.platform.menu.Menu;
import ua.com.fielden.platform.menu.MenuProducer;
import ua.com.fielden.platform.web.view.master.EntityMaster;

import com.google.inject.Injector;

public class MenuWebUiConfig {

    public final EntityMaster<Menu> master;

    public MenuWebUiConfig(final Injector injector) {
        master = createMaster(injector);
    }

    private static EntityMaster<Menu> createMaster(final Injector injector) {
        return new EntityMaster<>(Menu.class, MenuProducer.class, null, injector);
    }

}
