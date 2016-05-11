package ua.com.fielden.platform.web.view.master.api.compound;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;

public interface ICompoundMaster1<T extends AbstractEntity<?>, F extends AbstractFunctionalEntityWithCentreContext<T>> {
    /**
     * Specify the number of default menu item to be opened when compound master has been activated.
     * 
     * @param number
     * @return
     */
    ICompoundMaster2<T, F> andDefaultItemNumber(final int number);
}
