package ua.com.fielden.platform.entity.query;

import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Represents a retrieval model.
 * At a high level, this abstraction is very similar to that of fetch models, the difference being that the API of fetch
 * models is designed to be used by application developers, while this abstraction is used by the EQL-SQL transpiler and
 * the query execution engine.
 *
 * @param <T>  the entity type that can be retrieved with a retrieval model instance
 * @see ua.com.fielden.platform.entity.query.fluent.fetch
 */
public interface IRetrievalModel<T extends AbstractEntity<?>> {

    Class<T> getEntityType();

    /**
     * Indicates whether entity instances retrieved with this fetch model are expected to be instrumented.
     */
    boolean isInstrumented();

    Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> getRetrievalModels();

    Set<String> getPrimProps();

    Set<String> getProxiedProps();

    boolean containsProp(final String propName);

    boolean containsProxy(final String propName);

    /**
     * Indicates whether this fetch model is at the root of the entity graph. In other words, if this method returns
     * {@code true}, this fetch model is not a part of some other fetch model (i.e., there is no such fetch model whose
     * {@link #getRetrievalModels()} includes this fetch model.)
     */
    boolean topLevel();

    /**
     * Indicates whether this fetch model includes only such properties that are calculated and "for totals".
     */
    boolean containsOnlyTotals();

}
