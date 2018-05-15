package ua.com.fielden.platform.web.menu.item;

import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.view.master.EntityMaster;

public interface IModuleMenuConfig1 {

    IModuleMenuConfigWithView centre(EntityCentre<?> centre);

    IModuleMenuConfigWithView master(final EntityMaster<?> entityMaster);

    IModuleMenuConfigWithView view(final AbstractCustomView view);

    IModuleMenuConfig3 addMenuItem(String title);
}
