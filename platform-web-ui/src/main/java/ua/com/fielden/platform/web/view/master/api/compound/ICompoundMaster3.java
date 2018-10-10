package ua.com.fielden.platform.web.view.master.api.compound;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;

public interface ICompoundMaster3<T extends AbstractEntity<?>, F extends AbstractFunctionalEntityWithCentreContext<T>> {
    /**
     * Specify the icon for menu item.
     * 
     * @param icon
     * @return
     */
    ICompoundMaster4<T, F> icon(final String icon);
}
