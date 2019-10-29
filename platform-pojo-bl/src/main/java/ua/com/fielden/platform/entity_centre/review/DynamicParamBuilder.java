package ua.com.fielden.platform.entity_centre.review;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.from;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.is;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.not;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.to;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.prepCritValuesForEntityTypedProp;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.prepCritValuesForStringTypedProp;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteriaUtils.createNotInitialisedQueryProperty;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity_centre.exceptions.EntityCentreExecutionException;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * The utility class that is a responsible for creating the map between property names and it's values.
 *
 * @author TG Team
 *
 */
public class DynamicParamBuilder {
    private DynamicParamBuilder() {}

    /**
     * Creates and returns the map between {@link CritOnly} property names and their values.
     *
     * @param managedType
     * @param propertyNames
     */
    public static <T extends AbstractEntity<?>> Map<String, Object> buildParametersMap(final Class<T> managedType, final Map<String, Pair<Object, Object>> propValues) {
        final Map<String, Object> params = new HashMap<>();
        for (final Entry<String, Pair<Object, Object>> propValEntry : propValues.entrySet()) {
            final QueryProperty qp = createNotInitialisedQueryProperty(managedType, propValEntry.getKey());
            if (qp.isCritOnly()) {
                params.putAll(getPropertyValues(qp, propValEntry));
            }
        }
        return params;
    }

    /**
     * Returns the map between enhanced property names and it's values.
     *
     * @param qp
     * @param propValEntry
     * @return
     */
    private static Map<String, Object> getPropertyValues(final QueryProperty qp, final Entry<String, Pair<Object, Object>> propValEntry) {
        final Map<String, Object> pairVals = new HashMap<>();
        if (qp.isSingle()) {
            pairVals.put(propValEntry.getKey(), propValEntry.getValue().getKey());
        } else if (EntityUtils.isRangeType(qp.getType())) {
            pairVals.put(from(propValEntry.getKey()), propValEntry.getValue().getKey());
            pairVals.put(to(propValEntry.getKey()), propValEntry.getValue().getValue());
        } else if (EntityUtils.isBoolean(qp.getType())) {
            pairVals.put(is(propValEntry.getKey()), propValEntry.getValue().getKey());
            pairVals.put(not(propValEntry.getKey()), propValEntry.getValue().getValue());
        } else if (!qp.isSingle()) { // It is assumed that not SINGLE means MULTI
            if (EntityUtils.isEntityType(qp.getType())) {
                pairVals.put(propValEntry.getKey(), prepCritValuesForEntityTypedProp(convertToStringList(propValEntry.getValue().getKey())));
            } else if (EntityUtils.isString(qp.getType())) {
                pairVals.put(propValEntry.getKey(), prepCritValuesForStringTypedProp((String) propValEntry.getValue().getKey()));
            } else {
                throw new EntityCentreExecutionException(format("Selection criteria for property [%s] in type [%s] is not recognized as a valid.", propValEntry.getKey(), qp.getType().getName()));
            }
        }
        return pairVals;
    }

    /**
     * There are edge cases, such as when a domain model changes and {@code CritOnly} properties change from type {@code SINGLE} to {@code MULTI}, where the value passed is not a list, but a string.
     * It is important to handle such situations gracefully.
     *
     * @param key
     * @return
     */
    private static List<String> convertToStringList(final Object value) {
        if (value == null) {
            return emptyList();
        } else if (value instanceof List) {
            return (List<String>) value;
        } else {
            return listOf(value + "");
        }
    }
}
