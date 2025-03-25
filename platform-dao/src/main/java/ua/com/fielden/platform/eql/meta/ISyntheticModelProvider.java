package ua.com.fielden.platform.eql.meta;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import java.util.List;

/**
 * Provides access to models of synthetic entity types.
 * <p>
 * This interface will eventually replace other existing ways of accessing synthetic models, such as {@link EntityTypeInfo}.
 * It provides a higher level of abstraction, as not all synthetic entity types declare their models explicitly.
 * Some synthetic models are generated at runtime.
 * However, for backward compatibility it is ensured that all synthetic entity types store their models in a corresponding static field.
 * Generated models are reflectively assigned to static fields of synthetic entity types early at runtime.
 */
@ImplementedBy(SyntheticModelProvider.class)
public interface ISyntheticModelProvider {

    /**
     * Returns a list of synthetic models for the specified synthetic entity type.
     * <p>
     * For most synthetic entity types, models are declared explicitly in the type itself,
     * using a static field {@code EntityResultQueryModel model_} or {@code List<EntityResultQueryModel> models_}.
     * For synthetic audit-entity types, models are generated.
     * <p>
     * It is an error if the entity type is not of synthetic nature.
     */
    <E extends AbstractEntity<?>> List<EntityResultQueryModel<E>> getModels(Class<E> entityType);

}
