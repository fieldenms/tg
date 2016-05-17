package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.view.master.EntityMaster;

public interface IEntityActionBuilder0WithViews<T extends AbstractEntity<?>> extends IEntityActionBuilder0<T> {	
    /**
     * Specify the instance of unregistered embedded master that will be a view for this menu item.
     * 
     * @param embeddedMaster
     * @return
     */
    IEntityActionBuilder0<T> withView(final EntityMaster<?> embeddedMaster);
    /**
     * Specify the instance of unregistered embedded centre that will be a view for this menu item.
     * 
     * @param embeddedCentre
     * @return
     */
    IEntityActionBuilder0<T> withView(final EntityCentre<?> embeddedCentre);
}