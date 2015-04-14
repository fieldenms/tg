package ua.com.fielden.platform.web.menu.item;

import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.interfaces.IRenderable;

public interface IModuleMenuConfig1 {

    IModuleMenuConfig2 centre(EntityCentre<?> centre);

    IModuleMenuConfig2 view(final IRenderable view);
}
