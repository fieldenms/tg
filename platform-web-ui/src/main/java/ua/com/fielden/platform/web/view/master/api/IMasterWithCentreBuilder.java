package ua.com.fielden.platform.web.view.master.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.centre.IMasterWithCentre0;

/**
 * This contract is an entry point for Master with Centre API.
 *
 * @author TG Team
 *
 */
public interface IMasterWithCentreBuilder<T extends AbstractEntity<?>> {

    IMasterWithCentre0<T> forEntity(final Class<T> type);
    IMasterWithCentre0<T> forEntityWithSaveOnActivate(final Class<T> type);

}
