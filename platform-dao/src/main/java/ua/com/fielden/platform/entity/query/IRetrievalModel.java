package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a retrieval model.
 * At a high level, this abstraction is very similar to that of fetch models.
 * The difference is that the API of fetch models is designed to be used by application developers,
 * while this abstraction is used by the EQL-SQL transpiler and the query execution engine.
 *
 * @param <T>  the entity type that can be retrieved with a retrieval model instance
 * @see ua.com.fielden.platform.entity.query.fluent.fetch
 */
public sealed interface IRetrievalModel<T extends AbstractEntity<?>> permits EntityRetrievalModel, EntityAggregatesRetrievalModel {

    Class<T> getEntityType();

    /**
     * Indicates whether entity instances retrieved with this fetch model are expected to be instrumented.
     */
    boolean isInstrumented();

    Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> getRetrievalModels();

    /**
     * Returns a retrieval model located at the given path.
     * <p>
     * It is an error if there is no retrieval model at the given path.
     *
     * @param path  property path
     * @see #getRetrievalModelOpt(CharSequence)
     */
    IRetrievalModel<? extends AbstractEntity<?>> getRetrievalModel(CharSequence path);

    /**
     * An alternative to {@link #getRetrievalModel(CharSequence)} that returns an empty optional if there is no retrieval
     * model at the given path.
     */
    Optional<IRetrievalModel<? extends AbstractEntity<?>>> getRetrievalModelOpt(CharSequence path);

    Set<String> getPrimProps();

    Set<String> getProxiedProps();

    boolean containsProp(final String propName);

    boolean containsProxy(final String propName);

    /**
     * Indicates whether this retrieval model is at the root of the entity graph.
     * If this method returns {@code true}, this retrieval model is not a part of some other fetch model.
     * In other words, there is no such retrieval model whose {@link #getRetrievalModels()} includes this retrieval model.
     */
    boolean isTopLevel();

    /**
     * Creates a retrieval model that corresponds to the entity type that the fetch model is associated with.
     */
    static <E extends AbstractEntity<?>> IRetrievalModel<E> createRetrievalModel(
            final fetch<E> fetch,
            final IDomainMetadata domainMetadata,
            final QuerySourceInfoProvider qsip)
    {
        return fetch.getEntityType() == EntityAggregates.class
                ? (IRetrievalModel<E>) new EntityAggregatesRetrievalModel((fetch<EntityAggregates>) fetch, domainMetadata, qsip)
                : new EntityRetrievalModel<>(fetch, domainMetadata, qsip);
    }

}
