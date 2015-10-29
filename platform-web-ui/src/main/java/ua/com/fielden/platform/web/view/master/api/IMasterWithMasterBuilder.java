package ua.com.fielden.platform.web.view.master.api;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.view.master.api.with_master.IMasterWithMaster0;

/**
 * This contract is an entry point for a Master with Master API.
 *
 * @author TG Team
 *
 */
public interface IMasterWithMasterBuilder<T extends AbstractFunctionalEntityWithCentreContext<?>> {

    IMasterWithMaster0<T> forEntityWithSaveOnActivate(final Class<T> type);

}
