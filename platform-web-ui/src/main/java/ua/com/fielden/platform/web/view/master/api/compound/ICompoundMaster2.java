package ua.com.fielden.platform.web.view.master.api.compound;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCompoundMenuItem;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;

public interface ICompoundMaster2<T extends AbstractEntity<?>, F extends AbstractFunctionalEntityWithCentreContext<T>> {
    /**
     * Start menu item specification: specify the type of menu item functional entity.
     * 
     * @param type
     * @return
     */
    ICompoundMaster3<T, F> addMenuItem(final Class<? extends AbstractFunctionalEntityForCompoundMenuItem<T>> type);
}
