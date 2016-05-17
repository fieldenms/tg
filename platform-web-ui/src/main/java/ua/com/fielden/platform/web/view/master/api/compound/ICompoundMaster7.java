package ua.com.fielden.platform.web.view.master.api.compound;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;

public interface ICompoundMaster7<T extends AbstractEntity<?>, F extends AbstractFunctionalEntityWithCentreContext<T>> {
    /**
     * Specify additional menu item.
     * 
     * @return
     */
    ICompoundMaster2<T, F> also();
    /**
     * Completes building of compound master and registers the functional master that opens compound master.
     */
    void done();
}
