package ua.com.fielden.platform.web.menu.module;

import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.menu.IModuleMenuConfig;

public interface IModuleConfig4 {

    IModuleConfigDone centre(final EntityCentre<?> centre);

    IModuleConfigDone view(final IRenderable view);

    //TODO must provide master and also other custom views.

    IModuleMenuConfig menu();
}
