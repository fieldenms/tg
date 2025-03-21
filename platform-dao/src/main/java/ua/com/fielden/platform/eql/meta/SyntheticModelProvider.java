package ua.com.fielden.platform.eql.meta;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.utils.EntityUtils.findSyntheticModelFieldFor;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;

@Singleton
final class SyntheticModelProvider implements ISyntheticModelProvider {

    private static final String ERR_NO_MODEL_FOR_SYNTHETIC_ENTITY = "Could not obtain model(s) for synthetic entity [%s].";
    private static final String ERR_NON_SYNTHETIC_ENTITY_TYPE = "Cannot provide a synthetic model for non-synthetic entity type [%s].";
    private static final String ERR_MISSING_MODEL_FIELD =
            "Invalid synthetic entity [%s] definition: neither static field [model_] nor [models_] could be found.";

    private final Cache<Class<? extends AbstractEntity<?>>, List<? extends EntityResultQueryModel<?>>> cache =
            CacheBuilder.newBuilder().weakKeys().build();

    @Override
    @SuppressWarnings("unchecked")
    public <E extends AbstractEntity<?>> List<EntityResultQueryModel<E>> getModels(final Class<E> entityType) {
        final var baseEntityType = (Class<E>) PropertyTypeDeterminator.baseEntityType(entityType);

        try {
            return (List<EntityResultQueryModel<E>>) cache.get(baseEntityType, () -> createModels(baseEntityType));
        } catch (final ExecutionException ex) {
            throw new EntityException(ERR_NO_MODEL_FOR_SYNTHETIC_ENTITY.formatted(baseEntityType.getSimpleName()), ex.getCause());
        }
    }

    private <E extends AbstractEntity<?>> List<EntityResultQueryModel<E>> createModels(final Class<E> entityType) {
        if (isSyntheticEntityType(entityType)) {
            return modelsFromField(entityType);
        }
        else {
            throw new IllegalArgumentException(ERR_NON_SYNTHETIC_ENTITY_TYPE.formatted(entityType.getSimpleName()));
        }
    }

    /**
     * Returns a list of query models defined by a synthetic entity.
     */
    @SuppressWarnings("unchecked")
    private static <E extends AbstractEntity<?>> List<EntityResultQueryModel<E>> modelsFromField(final Class<E> entityType) {
        final var modelField = requireNonNull(findSyntheticModelFieldFor(entityType),
                                              () -> ERR_MISSING_MODEL_FIELD.formatted(entityType));
        try {
            final var name = modelField.getName();
            modelField.setAccessible(true);
            final Object value = modelField.get(null);
            if ("model_".equals(name)) {
                return ImmutableList.of((EntityResultQueryModel<E>) value);
            } else {
                // this must be "models_"
                return ImmutableList.copyOf((List<EntityResultQueryModel<E>>) modelField.get(null));
            }
        } catch (final Exception ex) {
            throw new EntityException(ERR_NO_MODEL_FOR_SYNTHETIC_ENTITY.formatted(entityType.getSimpleName()), ex);
        }
    }

}
