package ua.com.fielden.platform.entity_centre.review.criteria;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyException;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.DateTimeZone.getDefault;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isDoubleCriterion;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isPlaceholder;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.getDateValuesFrom;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isUtc;
import static ua.com.fielden.platform.utils.EntityUtils.isDate;
import static ua.com.fielden.platform.utils.Pair.pair;

/**
 * Provides utility methods for entity query criteria.
 *
 * @author TG Team
 *
 */
public class EntityQueryCriteriaUtils {

    public static Set<String> getAvailableProperties(Class<? extends AbstractEntity<?>> root, final Set<String> properties, IAuthorisationModel authorisationModel) {
        return properties.stream().filter(prop -> {
            if (StringUtils.isEmpty(prop)) {
                return true;
            }
            Authorise authAnnotation = AnnotationReflector.getPropertyAnnotation(Authorise.class, root, prop);
            if (authAnnotation != null && !authorisationModel.authorise(authAnnotation.value()).isSuccessful()) {
                return false;
            }
            return true;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Separates total properties from fetch properties. The key of the pair is the list of fetch properties, the value of the pair is the list of totals.
     *
     * @param root
     * @param tickManager
     * @param enhancer
     * @return
     */
    public static Pair<Set<String>, Set<String>> separateFetchAndTotalProperties(final Class<?> root, final IAddToResultTickManager tickManager, final IDomainTreeEnhancer enhancer) {
        final Set<String> fetchProperties = new LinkedHashSet<>();
        final Set<String> totalProperties = new LinkedHashSet<>();
        final Pair<List<Pair<String, Integer>>, Map<String, List<String>>> totalFetchProps = getMappedFetchAndTotals(root, tickManager, enhancer);
        for (final Pair<String, Integer> fetchProp : totalFetchProps.getKey()) {
            fetchProperties.add(fetchProp.getKey());
        }
        for (final List<String> totalProps : totalFetchProps.getValue().values()) {
            totalProperties.addAll(totalProps);
        }
        return pair(fetchProperties, totalProperties);
    }

    /**
     * Returns the pair of fetch properties and totals map. The totals map - it is a map between fetch properties and list of total names.
     *
     * @param rootType
     * @param cdtme
     * @return
     */
    public static Pair<List<Pair<String, Integer>>, Map<String, List<String>>> getMappedFetchAndTotals(final Class<?> root, final IAddToResultTickManager tickManager, final IDomainTreeEnhancer enhancer) {
        final List<Pair<String, Integer>> columns = new ArrayList<>();
        final Map<String, List<String>> totals = new HashMap<>();
        final List<String> checkedProperties = tickManager.checkedProperties(root);
        for (final String property : checkedProperties) {
            try {
                final ICalculatedProperty calcProperty = enhancer.getCalculatedProperty(root, property);
                if (calcProperty.category() == CalculatedPropertyCategory.AGGREGATED_EXPRESSION && calcProperty.getOriginationProperty() != null) {
                    final String originProperty = Reflector.fromRelative2AbsolutePath(calcProperty.getContextPath(), calcProperty.getOriginationProperty());
                    if (checkedProperties.contains(originProperty)) {
                        List<String> totalList = totals.get(originProperty);
                        if (totalList == null) {
                            totalList = new ArrayList<>();
                            totals.put(originProperty, totalList);
                        }
                        totalList.add(property);
                    }
                } else {
                    columns.add(pair(property, tickManager.getWidth(root, property)));
                }
            } catch (final IncorrectCalcPropertyException ex) {
                columns.add(pair(property, tickManager.getWidth(root, property)));
            }
        }
        return pair(columns, totals);
    }

    /**
     * Returns the map between real property name and pair of it's values. If the second value doesn't exists then it is null.
     *
     * @param root
     * @param managedType
     * @param tickManager
     * @return
     */
    public static Map<String, Pair<Object, Object>> createParamValuesMap(final Class<?> root, final Class<?> managedType, final IAddToCriteriaTickManager tickManager, final IDates dates) {
        final Map<String, Pair<Object, Object>> paramValues = new HashMap<>();
        for (final String propertyName : tickManager.checkedProperties(root)) {
            if (!isPlaceholder(propertyName)) {
                final boolean isEntityItself = "".equals(propertyName); // empty property means "entity itself"
                final boolean isDate = !isEntityItself && isDate(determinePropertyType(managedType, propertyName));
                if (isDoubleCriterion(managedType, propertyName)) {
                    if (isDate && tickManager.getDatePrefix(root, propertyName) != null && tickManager.getDateMnemonic(root, propertyName) != null) {
                        final Pair<Date, Date> fromAndTo = getDateValuesFrom(tickManager.getDatePrefix(root, propertyName), tickManager.getDateMnemonic(root, propertyName), tickManager.getAndBefore(root, propertyName), dates);
                        paramValues.put(propertyName, pair(
                            paramValue(fromAndTo.getKey(), isDate, managedType, propertyName),
                            paramValue(fromAndTo.getValue(), isDate, managedType, propertyName)
                        ));
                    } else {
                        paramValues.put(propertyName, pair(
                            paramValue(tickManager.getValue(root, propertyName), isDate, managedType, propertyName),
                            paramValue(tickManager.getValue2(root, propertyName), isDate, managedType, propertyName)
                        ));
                    }
                } else {
                    paramValues.put(propertyName, pair(
                        paramValue(tickManager.getValue(root, propertyName), isDate, managedType, propertyName),
                        null
                    ));
                }
            }
        }
        return paramValues;
    }

    /**
     * Converts {@code value} to a form suitable for EQL parameters.
     * <p>
     * The only specifics here is UTC date handling. Date value is converted to the form in which UTC dates are persisted in database.
     *
     * @param value
     * @param isDate -- {@code true} if property is of {@link Date} type
     * @param queryProperty
     * @return
     */
    public static Object paramValue(final Object value, final boolean isDate, final QueryProperty queryProperty) {
        return paramValue(value, isDate, queryProperty.getEntityClass(), queryProperty.getPropertyName());
    }

    /**
     * Converts {@code value} to a form suitable for EQL parameters.
     * <p>
     * The only specifics here is UTC date handling. Date value is converted to the form in which UTC dates are persisted in database.
     *
     * @param value
     * @param isDate -- {@code true} if property is of {@link Date} type
     * @param managedType
     * @param propertyName
     * @return
     */
    public static Object paramValue(final Object value, final boolean isDate, final Class<?> managedType, final String propertyName) {
        return value != null && isDate && isUtc(managedType, propertyName) ? utcDateParamValue((Date) value) : value;
    }

    /**
     * Converts {@code value} for UTC property to the form in which UTC dates are persisted in database.
     *
     * @param date
     * @return
     */
    private static Date utcDateParamValue(final Date date) {
        return new DateTime(date, UTC).withZoneRetainFields(getDefault()).toDate();
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
     * Returns the not configured query property instance for the specified property.
     *
     * @param propertyPath
     * @return
     */
    public static QueryProperty createNotInitialisedQueryProperty(final Class<?> root, final CharSequence propertyPath) {
        return new QueryProperty(root, propertyPath.toString());
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
