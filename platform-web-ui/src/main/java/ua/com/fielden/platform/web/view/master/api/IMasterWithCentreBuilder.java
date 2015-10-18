package ua.com.fielden.platform.web.view.master.api;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.view.master.api.with_centre.IMasterWithCentre0;

/**
 * This contract is an entry point for Master with Centre API.
 *
 * @author TG Team
 *
 */
public interface IMasterWithCentreBuilder<T extends AbstractFunctionalEntityWithCentreContext<?>> {

    IMasterWithCentre0<T> forEntityWithSaveOnActivate(final Class<T> type);

}
