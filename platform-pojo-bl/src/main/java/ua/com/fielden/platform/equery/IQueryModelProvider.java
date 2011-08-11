package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;

/**
 * A contract for all classes that want to provide a model for eQuery.
 * 
 * @author TG Team
 * 
 * @param <K>
 */
public interface IQueryModelProvider<T extends AbstractEntity> {

    /**
     * Provides a model.
     * 
     * @param parameterGetter
     *            - is used to determine parameters for query.
     * @return
     */
    public abstract IQueryModel<T> model(final IParameterGetter parameterGetter);
}
