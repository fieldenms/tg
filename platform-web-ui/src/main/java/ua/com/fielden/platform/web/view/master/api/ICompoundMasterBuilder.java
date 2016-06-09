package ua.com.fielden.platform.web.view.master.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.view.master.api.compound.ICompoundMaster0;

/**
 * This contract is an entry point for a Compound Master API.
 *
 * @author TG Team
 *
 */
public interface ICompoundMasterBuilder<T extends AbstractEntity<?>, F extends AbstractFunctionalEntityWithCentreContext<T>> {
    /**
     * Specify the functional entity type for which this compound master will be build.
     * 
     * @param type
     * @return
     */
    ICompoundMaster0<T, F> forEntity(final Class<F> type);

}
