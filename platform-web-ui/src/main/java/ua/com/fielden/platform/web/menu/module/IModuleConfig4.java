package ua.com.fielden.platform.web.menu.module;

import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.menu.IModuleMenuConfig;

/**
 * Used for specifying view (entity centre, master or custom view) for this module.
 *
 * @author TG Team
 *
 */
public interface IModuleConfig4 {

    IModuleConfigDone centre(final EntityCentre<?> centre);

    /**
     * Specifies custom view for this module config.
     *
     * @param view
     * @return
     */
    IModuleConfigDone view(final IRenderable view);

    //TODO must provide master and also other custom views.

    IModuleMenuConfig menu();
}
