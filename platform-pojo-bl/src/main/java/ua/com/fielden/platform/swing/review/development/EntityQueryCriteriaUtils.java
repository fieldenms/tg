package ua.com.fielden.platform.swing.review.development;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyException;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Provides utility methods for entity query criteria.
 * 
 * @author TG Team
 * 
 */
public class EntityQueryCriteriaUtils {

    /**
     * Separates total properties from fetch properties. The key of the pair is the list of fetch properties, the value of the pair is the list of totals.
     * 
     * @param root
     * @param tickManager
     * @param enhancer
     * @return
     */
    public static Pair<Set<String>, Set<String>> separateFetchAndTotalProperties(final Class<?> root, final IAddToResultTickManager tickManager, final IDomainTreeEnhancer enhancer) {
        final Set<String> fetchProperties = new HashSet<String>();
        final Set<String> totalProperties = new HashSet<String>();
        final Pair<List<Pair<String, Integer>>, Map<String, List<String>>> totalFetchProps = getMappedFetchAndTotals(root, tickManager, enhancer);
        for (final Pair<String, Integer> fetchProp : totalFetchProps.getKey()) {
            fetchProperties.add(fetchProp.getKey());
        }
        for (final List<String> totalProps : totalFetchProps.getValue().values()) {
            totalProperties.addAll(totalProps);
        }
        return new Pair<Set<String>, Set<String>>(fetchProperties, totalProperties);
    }

    /**
     * Returns the pair of fetch properties and totals map. The totals map - it is a map between fetch properties and list of total names.
     * 
     * @param rootType
     * @param cdtme
     * @return
     */
    public static Pair<List<Pair<String, Integer>>, Map<String, List<String>>> getMappedFetchAndTotals(final Class<?> root, final IAddToResultTickManager tickManager, final IDomainTreeEnhancer enhancer) {
        final List<Pair<String, Integer>> columns = new ArrayList<Pair<String, Integer>>();
        final Map<String, List<String>> totals = new HashMap<String, List<String>>();
        final List<String> checkedProperties = tickManager.checkedProperties(root);
        for (final String property : checkedProperties) {
            try {
                final ICalculatedProperty calcProperty = enhancer.getCalculatedProperty(root, property);
                if (calcProperty.category() == CalculatedPropertyCategory.AGGREGATED_EXPRESSION && calcProperty.getOriginationProperty() != null) {
                    final String originProperty = Reflector.fromRelative2AbsotulePath(calcProperty.getContextPath(), calcProperty.getOriginationProperty());
                    if (checkedProperties.contains(originProperty)) {
                        List<String> totalList = totals.get(originProperty);
                        if (totalList == null) {
                            totalList = new ArrayList<String>();
                            totals.put(originProperty, totalList);
                        }
                        totalList.add(property);
                    }
                } else {
                    columns.add(new Pair<String, Integer>(property, Integer.valueOf(tickManager.getWidth(root, property))));
                }
            } catch (final IncorrectCalcPropertyException ex) {
                columns.add(new Pair<String, Integer>(property, Integer.valueOf(tickManager.getWidth(root, property))));
            }
        }
        return new Pair<List<Pair<String, Integer>>, Map<String, List<String>>>(columns, totals);
    }

    /**
     * Returns the map between real property name and pair of it's values. If the second value doesn't exists then it is null.
     * 
     * @param root
     * @param managedType
     * @param tickManager
     * @return
     */
    public static Map<String, Pair<Object, Object>> createParamValuesMap(final Class<?> root, final Class<?> managedType, final IAddToCriteriaTickManager tickManager) {
        final Map<String, Pair<Object, Object>> paramValues = new HashMap<String, Pair<Object, Object>>();
        for (final String propertyName : tickManager.checkedProperties(root)) {
            if (!AbstractDomainTree.isPlaceholder(propertyName)) {
                if (AbstractDomainTree.isDoubleCriterionOrBoolean(managedType, propertyName)) {
                    if (EntityUtils.isDate(PropertyTypeDeterminator.determinePropertyType(managedType, propertyName)) && tickManager.getDatePrefix(root, propertyName) != null
                            && tickManager.getDateMnemonic(root, propertyName) != null) {
                        final Pair<Date, Date> fromAndTo = DynamicQueryBuilder.getDateValuesFrom(tickManager.getDatePrefix(root, propertyName), tickManager.getDateMnemonic(root, propertyName), tickManager.getAndBefore(root, propertyName));
                        paramValues.put(propertyName, new Pair<Object, Object>(fromAndTo.getKey(), fromAndTo.getValue()));
                    } else {
                        paramValues.put(propertyName, new Pair<Object, Object>(tickManager.getValue(root, propertyName), tickManager.getValue2(root, propertyName)));
                    }
                } else {
                    paramValues.put(propertyName, new Pair<Object, Object>(tickManager.getValue(root, propertyName), null));
                }
            }
        }
        return paramValues;
    }

    /**
     * Returns the not configured query property instance for the specified property.
     * 
     * @param propertyName
     * @return
     */
    public static QueryProperty createNotInitialisedQueryProperty(final Class<?> root, final String propertyName) {
        return new QueryProperty(root, propertyName);
    }

    /**
     * Returns the expression for calculated property specified with propName parameter. If the property is not calculated then returns <code>null</code>.
     * 
     * @param propName
     *            - the name of the calculated property.
     * @return
     */
    public static ExpressionModel getExpressionForProp(final Class<?> root, final String propName, final IDomainTreeEnhancer enhancer) {
        try {
            return enhancer.getCalculatedProperty(root, propName).getExpressionModel();
        } catch (final IncorrectCalcPropertyException e) {
            return null;
        }
    }
}
