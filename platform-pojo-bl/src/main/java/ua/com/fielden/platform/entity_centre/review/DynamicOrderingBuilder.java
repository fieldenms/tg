package ua.com.fielden.platform.entity_centre.review;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.utils.Pair;

import java.util.List;

import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.ASCENDING;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity_centre.exceptions.EntityCentreExecutionException.requireNotNullArgument;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteriaUtils.isPropertyAuthorised;

/// Utility class that is responsible for creating ordering models.
///
public class DynamicOrderingBuilder {

    /// Returns the ordering model for this query criteria.
    ///
    public static OrderingModel createOrderingModel(final Class<?> root, final List<Pair<String, Ordering>> orderedPairsWithUnauthorised) {
        final var orderedPairs = orderedPairsWithUnauthorised.stream()
            .filter(pair -> isPropertyAuthorised(root, pair.getKey()))
            .toList();
        requireNotNullArgument(root, "root");
        requireNotNullArgument(orderedPairs, "orderedPairs");
        IOrderingItemCloseable closeOrderable = null;
        for (final Pair<String, Ordering> orderPair : orderedPairs) {
            final var orderingItem = closeOrderable == null ? orderBy() : closeOrderable;
            final var part = orderingItem.yield(new DynamicPropertyAnalyser(root, orderPair.getKey()).getCriteriaFullName());
            closeOrderable = orderPair.getValue().equals(ASCENDING) ? part.asc() : part.desc();
        }
        return closeOrderable == null ? null : closeOrderable.model();
    }

}
