package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.view.master.EntityMaster;

public interface IEntityActionBuilder0<T extends AbstractEntity<?>> {	
    IEntityActionBuilder1<T> withContext(final CentreContextConfig contextConfig);
    
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