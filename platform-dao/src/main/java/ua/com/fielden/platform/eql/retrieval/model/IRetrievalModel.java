package ua.com.fielden.platform.eql.retrieval.model;

import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Internal representation of {@link ua.com.fielden.platform.entity.query.fluent.fetch}, enriched with metadata.
 *
 * @param <T> entity type
 */
public interface IRetrievalModel<T extends AbstractEntity<?>> {
    Class<T> getEntityType();

    boolean isInstrumented();

    Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> getRetrievalModels();

    Set<String> getPrimProps();

    Set<String> getProxiedProps();

    boolean containsProp(final String propName);

    boolean containsProxy(final String propName);

    boolean topLevel();

    boolean containsOnlyTotals();
}