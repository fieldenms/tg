package ua.com.fielden.platform.web.view.master.api.compound;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;

public interface ICompoundMaster4<T extends AbstractEntity<?>, F extends AbstractFunctionalEntityWithCentreContext<T>> {
    /**
     * Specify short description for menu item (title).
     * 
     * @param shortDesc
     * @return
     */
    ICompoundMaster5<T, F> shortDesc(final String shortDesc);
}
