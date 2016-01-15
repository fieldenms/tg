package ua.com.fielden.platform.web.menu.item;

import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;

public interface IModuleMenuConfig4 {

    IModuleMenuConfig5 centre(EntityCentre<?> centre);

    IModuleMenuConfig5 view(final AbstractCustomView view);
}
