package ua.com.fielden.platform.eql.meta;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.minheritance.MultiInheritanceEqlModelGenerator;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.utils.EntityUtils.*;

@Singleton
final class SyntheticModelProvider implements ISyntheticModelProvider {

    private static final String
            ERR_NO_MODEL_FOR_SYNTHETIC_ENTITY = "Could not obtain model(s) for synthetic entity [%s].",
            ERR_NON_SYNTHETIC_ENTITY_TYPE = "Cannot provide a synthetic model for non-synthetic entity type [%s].",
            ERR_MISSING_MODEL_FIELD = "Invalid synthetic entity [%s] definition: neither static field [model_] nor [models_] could be found.",
            ERR_GENERATOR_NOT_CONFIGURED = "Synthetic models for generated multi-inheritance entity types cannot be provided because the generator is not configured. The expected cause is EqlTestCase.";

    // Nullable because of EqlTestCase that needs to be refactored to use IoC.
    // Meanwhile, EqlTestCase will not be able to use synthetic models for audit types (it does not currently use them).
    private final @Nullable MultiInheritanceEqlModelGenerator multiInheritanceSynModelGenerator;

    private final Cache<Class<? extends AbstractEntity<?>>, List<? extends EntityResultQueryModel<?>>> cache =
            CacheBuilder.newBuilder().weakKeys().build();

    @Inject
    SyntheticModelProvider(final @Nullable MultiInheritanceEqlModelGenerator multiInheritanceEqlModelGenerator) {
        this.multiInheritanceSynModelGenerator = multiInheritanceEqlModelGenerator;
    }

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
            if (isGeneratedMultiInheritanceEntityType(entityType)) {
                if (multiInheritanceSynModelGenerator == null) {
                    throw new InvalidStateException(ERR_GENERATOR_NOT_CONFIGURED);
                }
                return multiInheritanceSynModelGenerator.generate(entityType);
            }
            else {
                return modelsFromField(entityType);
            }
        }
        else {
            throw new IllegalArgumentException(ERR_NON_SYNTHETIC_ENTITY_TYPE.formatted(entityType.getSimpleName()));
        }
    }

    /// Returns a list of query models defined by a synthetic entity.
    /// 
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
