package ua.com.fielden.platform.web.menu.module;

import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.menu.IModuleMenuConfig;

public interface IModuleConfig4 {

    IModuleConfigDone view(final IRenderable renderable);

    IModuleMenuConfig menu();
}
