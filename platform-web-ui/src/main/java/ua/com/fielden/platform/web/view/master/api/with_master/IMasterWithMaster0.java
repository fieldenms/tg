package ua.com.fielden.platform.web.view.master.api.with_master;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.helpers.IComplete;

/**
 * A contract for providing an entity master used by Master with Master.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMasterWithMaster0<T extends AbstractFunctionalEntityWithCentreContext<?>> {
    
    IComplete<T> withMaster(final EntityMaster<?> entityMaster);
    IComplete<T> withMasterAndWithNoParentCentreRefresh(final EntityMaster<?> entityMaster);
}
