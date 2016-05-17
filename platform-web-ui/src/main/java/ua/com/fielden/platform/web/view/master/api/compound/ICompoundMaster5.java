package ua.com.fielden.platform.web.view.master.api.compound;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;

public interface ICompoundMaster5<T extends AbstractEntity<?>, F extends AbstractFunctionalEntityWithCentreContext<T>> {
    /**
     * Specify long description for menu item (tooltip).
     * 
     * @param longDesc
     * @return
     */
    ICompoundMaster6<T, F> longDesc(final String longDesc);
}
