package ua.com.fielden.platform.entity.functional.centre;

import static ua.com.fielden.platform.reflection.ClassesRetriever.findClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IQueryEntity.class)
public class QueryEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = -4222560265655373848L;

    @IsProperty
    @MapTo
    @Title(value = "Entity type", desc = "Entity type")
    private String entityType;

    @IsProperty
    @MapTo
    @Title(value = "Criteria properties", desc = "Criteria properties")
    private Map<String, CritProp> criteriaProperties;

    @IsProperty
    @MapTo
    @Title(value = "Propertiers to fetch", desc = "Properties to fetch")
    private Map<String, FetchProp> fetchProperties;

    @Observable
    public QueryEntity setFetchProperties(final Map<String, FetchProp> fetchProperties) {
	this.fetchProperties = fetchProperties;
	return this;
    }

    public Map<String, FetchProp> getFetchProperties() {
	return fetchProperties;
    }

    @Observable
    public QueryEntity setCriteriaProperties(final Map<String, CritProp> criteriaProperties) {
	this.criteriaProperties = criteriaProperties;
	return this;
    }

    public Map<String, CritProp> getCriteriaProperties() {
	return criteriaProperties;
    }

    @Observable
    public QueryEntity setEntityType(final String entityType) {
	this.entityType = entityType;
	return this;
    }

    public String getEntityType() {
	return entityType;
    }

    @SuppressWarnings("unchecked")
    public Class<AbstractEntity<?>> getActualEntityType() {
	return (Class<AbstractEntity<?>>) findClass(entityType);
    }

    public List<QueryProperty> getQueryProperties() {
        final List<QueryProperty> queryProps = new ArrayList<>();
        for (final Map.Entry<String, CritProp> prop : criteriaProperties.entrySet()) {
            queryProps.add(createQueryProperty(prop));
        }
        return queryProps;
    }

    private QueryProperty createQueryProperty(final Entry<String, CritProp> prop) {
        final QueryProperty queryProp = EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(getActualEntityType(), prop.getKey());
        queryProp.setValue(prop.getValue().getValue1());
        queryProp.setValue2(prop.getValue().getValue2());
        return queryProp;
    }

    public Set<String> createFetchProps() {
        return fetchProperties.keySet();
    }

    public Set<String> createSummaryProps() {
        final Set<String> summaryProps = new HashSet<>();
        for (final FetchProp fetchProp : fetchProperties.values()) {
            summaryProps.addAll(fetchProp.getSummary());
        }
        return summaryProps;
    }

    public List<Pair<String, Ordering>> createOrderingProps() {
        final List<Pair<String, Ordering>> orderingList = new ArrayList<>();
        for (final Map.Entry<String, FetchProp> fetchProp : fetchProperties.entrySet()) {
            if (fetchProp.getValue().getOrdering() != null) {
                orderingList.add(new Pair<String, Ordering>(fetchProp.getKey(), fetchProp.getValue().getOrdering()));
            }
        }
        return orderingList;
    }

    public Map<String, Pair<Object, Object>> createParamMap() {
        final Map<String, Pair<Object, Object>> params = new HashMap<>();
        for (final Map.Entry<String, CritProp> critProp : criteriaProperties.entrySet()) {
            if (critProp != null) {
                params.put(critProp.getKey(), new Pair<>(critProp.getValue().getValue1(), critProp.getValue().getValue2()));
            }
        }
        return params;
    }
}