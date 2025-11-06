package ua.com.fielden.platform.entity_centre.review.criteria;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyException;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.DateTimeZone.getDefault;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isDoubleCriterion;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isPlaceholder;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.getDateValuesFrom;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.security.tokens.Template.READ;
import static ua.com.fielden.platform.security.tokens.TokenUtils.*;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isUtc;
import static ua.com.fielden.platform.utils.EntityUtils.isDate;
import static ua.com.fielden.platform.utils.Pair.pair;

/// Provides utility methods for entity query criteria.
///
public class EntityQueryCriteriaUtils {

    @Inject
    private static IAuthorisationModel authorisationModel;
    @Inject
    private static ISecurityTokenProvider securityTokenProvider;

    /// Returns the property names that the user is permitted to view or manipulate.
    /// The set of accessible properties is determined by the specified authorisation model.
    ///
    public static List<String> getAvailableProperties(Class<? extends AbstractEntity<?>> root, final List<String> properties) {
        return properties.stream()
            .filter(prop -> isPropertyAuthorised(root, prop))
            .collect(toList());
    }

    /// Determines whether the current user has permission to view a given property
    /// or use it as a selection criterion.
    /// The accessibility of the property is determined by the specified authorisation model.
    ///
    /// @param root     the root entity class
    /// @param property the property name to check; an empty string represents the root entity itself
    /// @return `true` if the property is authorised for reading, `false` otherwise.
    ///
    public static boolean isPropertyAuthorised(final Class<?> root, final String property) {
        // Root property (aka "entity itself") is always authorised.
        return property.isEmpty()
            // Non-root property access is governed by *_CanRead_property_* tokens, where the `property` part is never empty.
            || authorisePropertyReading(root, property, authorisationModel, securityTokenProvider).orElseGet(Result::successful).isSuccessful();
    }

    /// Creates an Entity Centre query for a list of [QueryProperty] and other parameters.
    ///
    /// This method is used in:
    ///  - CriteriaResource navigation, execution, and refreshing.
    ///  - Stream-based exporting.
    ///  - [ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer] actions,
    ///    where a subset of all criteria-conforming entities needs to be processed.
    ///
    /// This method is subject to Can Read security.
    ///
    public static <T extends AbstractEntity<?>> EntityQueryProgressiveInterfaces.ICompleted<T> createCompletedQuery(
        final Class<T> type,
        final Class<T> managedType,
        final List<QueryProperty> queryProperties,
        final Optional<IQueryEnhancer<T>> additionalQueryEnhancer,
        final Optional<CentreContext<T, ?>> centreContextForQueryEnhancer,
        final Optional<User> createdByUserConstraint,
        final IDates dates)
    {
        authoriseReading(type.getSimpleName(), READ, authorisationModel, securityTokenProvider).ifFailure(Result::throwRuntime);
        authoriseCriteria(queryProperties, authorisationModel, securityTokenProvider).ifFailure(Result::throwRuntime);
        if (createdByUserConstraint.isPresent()) {
            final QueryProperty queryProperty = EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(managedType, "createdBy");
            final List<String> createdByCriteria = new ArrayList<>();
            createdByCriteria.add(createdByUserConstraint.get().toString());
            queryProperty.setValue(createdByCriteria);
            queryProperties.add(queryProperty);
        }
        if (additionalQueryEnhancer.isPresent()) {
            return DynamicQueryBuilder.createQuery(managedType, queryProperties, Optional.of(new Pair<>(additionalQueryEnhancer.get(), centreContextForQueryEnhancer)), dates);
        } else {
            return DynamicQueryBuilder.createQuery(managedType, queryProperties, dates);
        }
    }

    /// Separates total properties from fetch properties.
    /// The key of the returned pair is the list of fetch properties,
    /// and the value is the list of total properties.
    ///
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

    /// Returns a pair consisting of fetch properties and a totals map.
    /// The totals map is a mapping between fetch properties and their corresponding lists of total names.
    ///
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
                        final var totalList = totals.computeIfAbsent(originProperty, k -> new ArrayList<>());
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

    /// Returns a map between real property names and a `pair` of their values.
    /// If the second value in the pair does not exist, it will be `null`.
    ///
    public static Map<String, Pair<Object, Object>> createParamValuesMap(final Class<?> root, final Class<?> managedType, final IAddToCriteriaTickManager tickManager, final IDates dates) {
        final Map<String, Pair<Object, Object>> paramValues = new HashMap<>();
        for (final String propertyName : tickManager.checkedProperties(root)) {
            if (!isPlaceholder(propertyName)) {
                final boolean isEntityItself = propertyName.isEmpty(); // empty property means "entity itself"
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

    /// Converts `value` to a form suitable for EQL parameters.
    ///
    /// The only specific handling here is for UTC dates.
    /// Date values are converted to the format in which UTC dates are persisted in the database.
    ///
    /// @param isDate `true` if the property is of type [Date]
    ///
    public static Object paramValue(final Object value, final boolean isDate, final QueryProperty queryProperty) {
        return paramValue(value, isDate, queryProperty.getEntityClass(), queryProperty.getPropertyName());
    }

    /// Converts `value` to a form suitable for EQL parameters.
    ///
    /// The only special handling is for UTC dates.
    /// Date values are converted to the form in which UTC dates are persisted in the database.
    ///
    /// @param isDate `true` if the property is of type [Date]
    ///
    public static Object paramValue(final Object value, final boolean isDate, final Class<?> managedType, final String propertyName) {
        return value != null && isDate && isUtc(managedType, propertyName) ? utcDateParamValue((Date) value) : value;
    }

    /// Converts a UTC `value` to the form used for persisting UTC dates in the database.
    ///
    private static Date utcDateParamValue(final Date date) {
        return new DateTime(date, UTC).withZoneRetainFields(getDefault()).toDate();
    }

    /// Returns an unconfigured query property instance for the specified property.
    ///
    public static QueryProperty createNotInitialisedQueryProperty(final Class<?> root, final String propertyName) {
        return new QueryProperty(root, propertyName);
    }

    /// Returns an unconfigured query property instance for the specified property.
    ///
    public static QueryProperty createNotInitialisedQueryProperty(final Class<?> root, final CharSequence propertyPath) {
        return new QueryProperty(root, propertyPath.toString());
    }

    /// Returns the expression for the calculated property specified by the `propName` parameter.
    /// Returns `null` if the property is not calculated.
    ///
    /// @param propName the name of the calculated property
    ///
    public static ExpressionModel getExpressionForProp(final Class<?> root, final String propName, final IDomainTreeEnhancer enhancer) {
        try {
            return enhancer.getCalculatedProperty(root, propName).getExpressionModel();
        } catch (final IncorrectCalcPropertyException e) {
            return null;
        }
    }

}
