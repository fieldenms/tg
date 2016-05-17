package ua.com.fielden.platform.web.view.master.api.compound;

import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;

public interface ICompoundMaster0<T extends AbstractEntity<?>, F extends AbstractFunctionalEntityWithCentreContext<T>> {
    /**
     * Specify producer type for functional entity which will open this compound master.
     * 
     * @param producerType
     * @return
     */
    ICompoundMaster1<T, F> withProducer(final Class<? extends IEntityProducer<F>> producerType);
}
