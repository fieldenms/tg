package ua.com.fielden.platform.web.view.master.api.compound;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;

/**
 * A contract to provide additional information about the collectional property which entities are represented in embedded entity centre.
 *
 * @author TG Team
 *
 */
public interface ICompoundMaster6CentreView<T extends AbstractEntity<?>, F extends AbstractFunctionalEntityWithCentreContext<T>> {

    /**
     * Specifies the collectional property which entities are represented by embedded entity centre.
     *
     * @param property
     * @return
     */
    ICompoundMaster7<T, F>represents(final String property);
}
