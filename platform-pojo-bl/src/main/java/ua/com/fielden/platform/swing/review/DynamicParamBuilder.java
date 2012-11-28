package ua.com.fielden.platform.swing.review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.from;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.is;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.not;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.to;
import static ua.com.fielden.platform.swing.review.DynamicQueryBuilder.prepare;

/**
 * The utility class that is a responsible for creating the map between property names and it's values.
 *
 * @author TG Team
 *
 */
public class DynamicParamBuilder {

    /**
     * Creates and returns the map between {@link CritOnly} property names and their values.
     *
     * @param managedType
     * @param propertyNames
     */
    public static <T extends AbstractEntity<?>> Map<String, Object> buildParametersMap(final Class<T> managedType, final Map<String, Pair<Object, Object>> propValues){
	final Map<String, Object> params = new HashMap<String, Object>();
	for (final Entry<String, Pair<Object, Object>> propValEntry : propValues.entrySet()) {
	    final QueryProperty qp = EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(managedType, propValEntry.getKey());
	    params.putAll(getPropertyValues(qp, propValEntry));
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
	final Map<String, Object> pairVals = new HashMap<String, Object>();
	if (qp.isCritOnly()) {
	    if (qp.isSingle()) {
		pairVals.put(propValEntry.getKey(), propValEntry.getValue().getKey());
	    } else if (EntityUtils.isRangeType(qp.getType())) {
		pairVals.put(from(propValEntry.getKey()), propValEntry.getValue().getKey());
		pairVals.put(to(propValEntry.getKey()), propValEntry.getValue().getValue());
	    } else if (EntityUtils.isBoolean(qp.getType())) {
		pairVals.put(is(propValEntry.getKey()), propValEntry.getValue().getKey());
		pairVals.put(not(propValEntry.getKey()), propValEntry.getValue().getValue());
	    } else if (!qp.isSingle() && EntityUtils.isEntityType(qp.getType())) { // It is assumed that not SINGLE means RANGE
		pairVals.put(propValEntry.getKey(), prepare((List<String>) propValEntry.getValue().getKey()));
	    }
	}
	return pairVals;
    }
}
