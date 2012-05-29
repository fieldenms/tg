package ua.com.fielden.platform.swing.review.development;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyKeyException;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.ITickManager;
import ua.com.fielden.platform.domaintree.centre.IOrderingManager;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.utils.Pair;

/**
 * Provides utility methods for entity query criteria.
 *
 * @author TG Team
 *
 */
public class EntityQueryCriteriaUtils {

    /**
     * Returns the ordered pair of object and ordering value for the specified ordering manager and enhancer. Object might be property name or expression model.
     *
     * @param root
     * @param orderingManager
     * @param enhancer
     * @return
     */
    public static List<Pair<Object, Ordering>> getOrderingList(final Class<?> root, final IOrderingManager orderingManager, final IDomainTreeEnhancer enhancer){
	final List<Pair<Object, Ordering>> orderingPairs = new ArrayList<Pair<Object,Ordering>>();
	for(final Pair<String, Ordering> orderPair : orderingManager.orderedProperties(root)){
	    final ExpressionModel expression = getExpressionForProp(root, orderPair.getKey(), enhancer);
	    orderingPairs.add(new Pair<Object, Ordering>((expression == null ? orderPair.getKey() : expression), orderPair.getValue()));
	}
	return orderingPairs;
    }

    /**
     * Separates total properties from fetch properties. The key of the pair is the list of fetch properties, the value of the pair is the list of totals.
     *
     * @param root
     * @param tickManager
     * @param enhancer
     * @return
     */
    public static Pair<Set<String>, Set<String>> separateFetchAndTotalProperties(final Class<?> root, final ITickManager tickManager, final IDomainTreeEnhancer enhancer) {
        final Set<String> fetchProperties = new HashSet<String>();
        final Set<String> totalProperties = new HashSet<String>();
        final List<String> checkedProperties = tickManager.checkedProperties(root);
        for (final String property : checkedProperties)
            try {
        	final ICalculatedProperty calcProperty = enhancer.getCalculatedProperty(root, property);
        	final String originProperty = Reflector.fromRelative2AbsotulePath(calcProperty.getContextPath(), calcProperty.getOriginationProperty());
		if(calcProperty.category() == CalculatedPropertyCategory.AGGREGATED_EXPRESSION){
		    if(checkedProperties.contains(originProperty)){
			totalProperties.add(property);
		    }
		} else {
        	    fetchProperties.add(property);
        	}
            } catch (final IncorrectCalcPropertyKeyException ex) {
        	fetchProperties.add(property);
            }
        return new Pair<Set<String>, Set<String>>(fetchProperties, totalProperties);
    }

    /**
     * Returns the not configured query property instance for the specified property.
     *
     * @param propertyName
     * @return
     */
    public static QueryProperty createNotInitialisedQueryProperty(final Class<?> root, final String propertyName){
	return new QueryProperty(root, propertyName);
    }

    /**
     * Returns the expression for calculated property specified with propName parameter. If the property is not calculated then returns null.
     *
     * @param propName - the name of the calculated property.
     * @return
     */
    private static ExpressionModel getExpressionForProp(final Class<?> root, final String propName, final IDomainTreeEnhancer enhancer) {
	try {
	    return ((CalculatedProperty) enhancer.getCalculatedProperty(root, propName)).getAst().getModel();
	} catch (final IncorrectCalcPropertyKeyException e) {
	    return null;
	}
    }
}
