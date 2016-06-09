package ua.com.fielden.platform.web.view.master.api.compound;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.view.master.EntityMaster;

public interface ICompoundMaster6<T extends AbstractEntity<?>, F extends AbstractFunctionalEntityWithCentreContext<T>> {
    /**
     * Specify the instance of unregistered embedded master that will be a view for this menu item.
     * 
     * @param embeddedMaster
     * @return
     */
    ICompoundMaster7<T, F> withView(final EntityMaster<?> embeddedMaster);
    /**
     * Specify the instance of unregistered embedded centre that will be a view for this menu item.
     * 
     * @param embeddedCentre
     * @return
     */
    ICompoundMaster7<T, F> withView(final EntityCentre<?> embeddedCentre);
}
