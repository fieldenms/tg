package ua.com.fielden.platform.entity.query;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.meta.IDomainMetadata;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.NONE;
import static ua.com.fielden.platform.utils.EntityUtils.splitPropPathToArray;

/**
 * Represents a retrieval model specialised for {@link EntityAggregates}.
 * The following constraints apply to this type of retrieval models:
 * <ul>
 *   <li> The only valid fetch category is {@link FetchCategory#NONE}.
 *   <li> It is illegal for a fetch model to specify excluded properties.
 *   <li> It is illegal for a fetch model to be empty.
 * </ul>
 * Also, all retrieval models of this type have the following properties:
 * <ul>
 *   <li> They are always top-level.
 *   <li> The set of proxied properties is always empty.
 *   <li> {@link #containsOnlyTotals()} is always false.
 * </ul>
 */
public final class EntityAggregatesRetrievalModel implements IRetrievalModel<EntityAggregates> {

    private final Set<String> primProps;
    private final Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> entityProps;
    private final fetch<EntityAggregates> originalFetch;

    public EntityAggregatesRetrievalModel(
            final fetch<EntityAggregates> originalFetch,
            final IDomainMetadata domainMetadata,
            final QuerySourceInfoProvider qsip)
    {
        validateFetch(originalFetch);
        this.originalFetch = originalFetch;
        this.primProps = ImmutableSet.copyOf(originalFetch.getIncludedProps());
        this.entityProps = buildEntityModels(originalFetch.getIncludedPropsWithModels(), domainMetadata, qsip);
    }

    @Override
    public Class<EntityAggregates> getEntityType() {
        return EntityAggregates.class;
    }

    @Override
    public boolean isInstrumented() {
        return originalFetch.isInstrumented();
    }

    @Override
    public Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> getRetrievalModels() {
        return entityProps;
    }

    @Override
    public IRetrievalModel<? extends AbstractEntity<?>> getRetrievalModel(final CharSequence path) {
        final var model = getRetrievalModelOrNull(path);
        if (model == null) {
            throw new EqlException(format("No such property [%s] in retrieval model:\n%s", path, this));
        }
        return model;
    }

    @Override
    public Optional<IRetrievalModel<? extends AbstractEntity<?>>> getRetrievalModelOpt(final CharSequence path) {
        return Optional.ofNullable(getRetrievalModelOrNull(path));
    }

    private @Nullable IRetrievalModel<?> getRetrievalModelOrNull(final CharSequence path) {
        final var names = splitPropPathToArray(path);

        IRetrievalModel<?> model = this;
        for (int i = 0; i < names.length && model != null; i++) {
            model = model.getRetrievalModels().get(names[i]);
        }

        return model;
    }

    @Override
    public Set<String> getPrimProps() {
        return primProps;
    }

    @Override
    public Set<String> getProxiedProps() {
        return ImmutableSet.of();
    }

    @Override
    public boolean containsProp(final String propName) {
        return primProps.contains(propName) || entityProps.containsKey(propName);
    }

    @Override
    public boolean containsProxy(final String propName) {
        return false;
    }

    @Override
    public boolean topLevel() {
        return true;
    }

    private static void validateFetch(final fetch<EntityAggregates> originalFetch) {
        if (NONE != originalFetch.getFetchCategory()) {
            throw new EqlException("""
                    The only acceptable category for EntityAggregates entity type fetch model is NONE. \
                    Use EntityQueryUtils.fetchAggregates(..) method to create a correct fetch model.""");
        }

        if (!originalFetch.getExcludedProps().isEmpty()) {
            throw new EqlException("The possibility to exclude certain properties cannot be be applied for EntityAggregates entity type fetch model.");
        }

        if (originalFetch.getIncludedPropsWithModels().isEmpty() && originalFetch.getIncludedProps().isEmpty()) {
            throw new EqlException("Fetch model for EntityAggregates entity type must not be empty.");
        }
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("Fetch model:\n------------------------------------------------\n");
        sb.append("\t original:\n" + originalFetch + "\n\n");
        sb.append(primProps);
        if (entityProps.size() > 0) {
            sb.append("\n------------------------------------------------");
            for (final Map.Entry<String, EntityRetrievalModel<? extends AbstractEntity<?>>> fetchEntry : entityProps.entrySet()) {
                sb.append("\n" + fetchEntry.getKey() + " <<< " + fetchEntry.getValue());
                sb.append("\n------------------------------------------------");
            }
        }

        return sb.toString();
    }

    private static Map<String, EntityRetrievalModel<?>> buildEntityModels(
            final Map<String, fetch<? extends AbstractEntity<?>>> models,
            final IDomainMetadata domainMetadata,
            final QuerySourceInfoProvider qsip)
    {
        if (models.isEmpty()) {
            return ImmutableMap.of();
        } else {
            final var map = new HashMap<String, EntityRetrievalModel<?>>(models.size());
            models.forEach((prop, model) -> {
                final var existingModel = map.get(prop);
                final var finalFetch = existingModel != null ? existingModel.getOriginalFetch().unionWith(model) : model;
                map.put(prop, new EntityRetrievalModel<>(finalFetch, domainMetadata, qsip, false));
            });
            return unmodifiableMap(map);
        }
    }

}
