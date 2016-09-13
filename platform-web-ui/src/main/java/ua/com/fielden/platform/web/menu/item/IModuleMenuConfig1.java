package ua.com.fielden.platform.web.menu.item;

import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;

public interface IModuleMenuConfig1 {

    IModuleMenuConfigWithView centre(EntityCentre<?> centre);

    IModuleMenuConfigWithView view(final AbstractCustomView view);

    IModuleMenuConfig2 submenu();
}
