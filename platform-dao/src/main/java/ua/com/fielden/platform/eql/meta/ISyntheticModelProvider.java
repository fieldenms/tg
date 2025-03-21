package ua.com.fielden.platform.eql.meta;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import java.util.List;

/**
 * Provides access to models of synthetic entity types.
 */
@ImplementedBy(SyntheticModelProvider.class)
public interface ISyntheticModelProvider {

    /**
     * Returns a list of models as defined in the specified entity type.
     * <p>
     * It is an error if the entity type is not of synthetic nature.
     */
    <E extends AbstractEntity<?>> List<EntityResultQueryModel<E>> getModels(Class<E> entityType);

}
