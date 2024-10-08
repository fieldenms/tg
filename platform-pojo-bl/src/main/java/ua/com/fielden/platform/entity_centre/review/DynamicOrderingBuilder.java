package ua.com.fielden.platform.entity_centre.review;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity_centre.exceptions.EntityCentreExecutionException;
import ua.com.fielden.platform.utils.Pair;

import java.util.List;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;

/**
 * Utility class that is responsible for creating ordering models.
 * 
 * @author TG Team
 * 
 */
public class DynamicOrderingBuilder {

    /**
     * Returns the ordering model for this query criteria.
     * 
     * @return
     */
    public static OrderingModel createOrderingModel(final Class<?> root, final List<Pair<String, Ordering>> orderedPairs) {
        EntityCentreExecutionException.requireNotNullArgument(root, "root");
        EntityCentreExecutionException.requireNotNullArgument(orderedPairs, "orderedPairs");
        IOrderingItemCloseable closeOrderable = null;
        for (final Pair<String, Ordering> orderPair : orderedPairs) {
            final IOrderingItem orderingItem = closeOrderable == null ? orderBy() : closeOrderable;
            final DynamicPropertyAnalyser analyser = new DynamicPropertyAnalyser(root, orderPair.getKey());
            final ISingleOperandOrderable part = orderingItem.yield(analyser.getCriteriaFullName());
            closeOrderable = orderPair.getValue().equals(Ordering.ASCENDING) ? part.asc() : part.desc();
        }
        return closeOrderable == null ? null : closeOrderable.model();
    }

}
