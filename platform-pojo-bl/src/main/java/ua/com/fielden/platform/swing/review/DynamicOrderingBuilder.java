package ua.com.fielden.platform.swing.review;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;

import java.util.List;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Utility class that is responsible for creating ordering model.
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
    public static OrderingModel createOrderingModel(final Class<?> root, final List<Pair<Object, Ordering>> orderedPairs){
	if(root == null || orderedPairs == null){
	    throw new NullPointerException("The root or orderedPirs parameters can not be null");
	}
	IOrderingItemCloseable closeOrderable = null;
	for(final Pair<Object, Ordering> orderPair : orderedPairs){
	    final IOrderingItem orderingItem = closeOrderable == null ? orderBy() : closeOrderable;
	    final ISingleOperandOrderable part = orderPair.getKey() instanceof ExpressionModel ? orderingItem.expr((ExpressionModel)orderPair.getKey()) //
		    : orderingItem.prop(EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(root, (String)orderPair.getKey()).getConditionBuildingName());
	    closeOrderable = orderPair.getValue().equals(Ordering.ASCENDING) ? part.asc() : part.desc();
	}
	return closeOrderable == null ? null : closeOrderable.model();
    }

}
