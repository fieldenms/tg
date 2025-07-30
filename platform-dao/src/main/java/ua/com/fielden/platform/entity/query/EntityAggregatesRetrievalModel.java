package ua.com.fielden.platform.entity.query;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString.IFormat;
import ua.com.fielden.platform.utils.ToString.IFormattable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.NONE;
import static ua.com.fielden.platform.utils.EntityUtils.splitPropPathToArray;
import static ua.com.fielden.platform.utils.ToString.separateLines;

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
 * </ul>
 */
public final class EntityAggregatesRetrievalModel implements IRetrievalModel<EntityAggregates>, IFormattable {

    public static final String ERR_UNEXPECTED_PROPERTY_IN_RETRIEVAL_MODEL = "No property [%s] in retrieval model:%n%s";
    public static final String ERR_ONLY_FETCH_CATEGORY_NONE_IS_SUPPORTED = """
            The only acceptable category for EntityAggregates entity type fetch model is NONE. \
            Use EntityQueryUtils.fetchAggregates(..) method to create a correct fetch model.""";
    public static final String ERR_PROPERTY_EXCLUSION_IS_UNSUPPORTED = "Property exclusion is not applicable to EntityAggregates.";
    public static final String ERR_EMPTY_FETCH_MODEL = "Fetch model for EntityAggregates must not be empty.";

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
            throw new EqlException(ERR_UNEXPECTED_PROPERTY_IN_RETRIEVAL_MODEL.formatted(path, this));
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
    public boolean isTopLevel() {
        return true;
    }

    private static void validateFetch(final fetch<EntityAggregates> originalFetch) {
        if (NONE != originalFetch.getFetchCategory()) {
            throw new EqlException(ERR_ONLY_FETCH_CATEGORY_NONE_IS_SUPPORTED);
        }

        if (!originalFetch.getExcludedProps().isEmpty()) {
            throw new EqlException(ERR_PROPERTY_EXCLUSION_IS_UNSUPPORTED);
        }

        if (originalFetch.getIncludedPropsWithModels().isEmpty() && originalFetch.getIncludedProps().isEmpty()) {
            throw new EqlException(ERR_EMPTY_FETCH_MODEL);
        }
    }

    @Override
    public String toString() {
        return toString(separateLines());
    }

    @Override
    public String toString(final IFormat format) {
        return format.toString(this)
                .add("category", originalFetch.getFetchCategory())
                .addIfNotEmpty("primitives", primProps)
                .addIfNotEmpty("subModels", entityProps)
                .$();
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
