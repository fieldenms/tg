package ua.com.fielden.platform.swing.review.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.HqlDateFunctions;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompleted;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompletedAndOrdered;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompletedAndYielded;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reportquery.AggregationProperty;
import ua.com.fielden.platform.reportquery.DistributionDateProperty;
import ua.com.fielden.platform.reportquery.DistributionProperty;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.review.EntityQueryCriteriaExtender;

public class PivotAnalysisQueryExtender<T extends AbstractEntity, DAO extends IEntityDao<T>> extends EntityQueryCriteriaExtender<T, DAO, List<EntityAggregates>> {

    private final Map<IDistributedProperty, String> aliases = new HashMap<IDistributedProperty, String>();

    private List<IDistributedProperty> distributionProperties;
    private List<IAggregatedProperty> aggregationProperties;

    public List<IDistributedProperty> getDistributionProperties() {
	return distributionProperties != null ? Collections.unmodifiableList(distributionProperties) : new ArrayList<IDistributedProperty>();
    }

    public void setDistributionProperties(final List<IDistributedProperty> distributionProperties) {
	this.distributionProperties = distributionProperties;
    }

    public List<IAggregatedProperty> getAggregationProperties() {
	return aggregationProperties != null ? Collections.unmodifiableList(aggregationProperties) : new ArrayList<IAggregatedProperty>();
    }

    public void setAggregationProperties(final List<IAggregatedProperty> aggregationProperties) {
	this.aggregationProperties = aggregationProperties;
    }

    protected ICompletedAndOrdered createExtendedQuery() {
	initAliases();
	if (aliases.size() == 0 || getDistributionProperties() == null || getDistributionProperties().isEmpty()) {
	    return null;
	} else {
	    ICompleted queryBase = getBaseQueryModel();
	    for (final IDistributedProperty distributionProperty : getDistributionProperties()) {
		distributionProperty.setTableAlias(getBaseCriteria().getAlias());
		final String groupParsedParameter = distributionProperty.getParsedValue();
		if (!StringUtils.isEmpty(groupParsedParameter)) {
		    queryBase = distributionProperty.isExpression() ? queryBase.groupByExp(groupParsedParameter) //
			    : queryBase.groupByProp(groupParsedParameter);
		}

	    }
	    if (queryBase == null) {
		return null;
	    }
	    ICompletedAndYielded queryFinalBase = queryBase;
	    for (final Iterator<Map.Entry<IDistributedProperty, String>> iterator = aliases.entrySet().iterator(); iterator.hasNext();) {
		final Map.Entry<IDistributedProperty, String> yieldEntry = iterator.next();
		final IDistributedProperty aggregated = yieldEntry.getKey();
		aggregated.setTableAlias(getBaseCriteria().getAlias());
		if (aggregated.isExpression()) {
		    queryFinalBase = queryFinalBase.yieldExp(aggregated.getParsedValue(), yieldEntry.getValue());
		} else {
		    queryFinalBase = queryFinalBase.yieldProp(aggregated.getParsedValue(), yieldEntry.getValue());
		}
	    }
	    return queryFinalBase;

	    //	    String groupParameter = null;
	    //	    Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(getBaseCriteria().getEntityClass(), getDistributionProperties().getActualProperty());
	    //	    if (AbstractEntity.class.isAssignableFrom(propertyType)) {
	    //		groupParameter = getDistributionProperties().getActualProperty();
	    //		while (AbstractEntity.class.isAssignableFrom(propertyType)) {
	    //		    groupParameter += ".key";
	    //		    propertyType = PropertyTypeDeterminator.determinePropertyType(getBaseCriteria().getEntityClass(), groupParameter);
	    //		}
	    //	    } else {
	    //		groupParameter = getAliasForDistributionProperty();
	    //	    }
	    //	    if (getSortingProperty() == null || getSortOrder() == SortOrder.UNSORTED || StringUtils.isEmpty(getSortingPropertyAlias())) {
	    //		final String orderQueryPrefix = StringUtils.isEmpty(getBaseCriteria().getAlias()) ? "" : getBaseCriteria().getAlias() + ".";
	    //		return queryFinalBase.orderBy(orderQueryPrefix + groupParameter);
	    //	    } else {
	    //		return queryFinalBase.orderBy(getSortingPropertyAlias() + " " + (getSortOrder() == SortOrder.ASCENDING ? "asc" : "desc"));
	    //	    }
	}
    }

    protected fetch<EntityAggregates> createExtendedFetchModel() {
	fetch<EntityAggregates> fetchModel = new fetch<EntityAggregates>(EntityAggregates.class);
	for (final IDistributedProperty distributionProperty : getDistributionProperties()) {
	    final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(getBaseCriteria().getEntityClass()//
	    , distributionProperty.getActualProperty());
	    if (AbstractEntity.class.isAssignableFrom(propertyType)) {
		fetchModel = fetchModel.with(getAliasFor(distributionProperty), new fetch(propertyType));
	    }

	}
	return fetchModel;
    }

    protected List<EntityAggregates> getQueryResult(final IQueryOrderedModel<EntityAggregates> queryModel, final fetch<EntityAggregates> fetchModel, final int pageSize) {
	return getBaseCriteria().getEntityAggregatesDao().listAggregates(queryModel, fetchModel);
    }

    private void initAliases() {
	int aliasCounter = 0;
	aliases.clear();
	final List<IAggregatedProperty> aggregationsToAdd = new ArrayList<IAggregatedProperty>();
	final List<IDistributedProperty> distributionsToAdd = new ArrayList<IDistributedProperty>();
	if (aggregationProperties != null) {
	    for (final IAggregatedProperty aggregationProperty : aggregationProperties) {
		if (aggregationProperty.getAggregationFunction() == AnalysisPropertyAggregationFunction.AVG) {
		    final IAggregatedProperty aggregation = createAggregationPropertyFor(aggregationProperty, AnalysisPropertyAggregationFunction.COUNT);
		    aggregationsToAdd.add(aggregation);
		    aliases.put(aggregation, "pivot_report_alias_" + Integer.toString(aliasCounter++));
		}
		if (aggregationProperty.getAggregationFunction() != AnalysisPropertyAggregationFunction.DISTINCT_COUNT) {
		    aliases.put(aggregationProperty, "pivot_report_alias_" + Integer.toString(aliasCounter++));
		} else {
		    distributionsToAdd.add(createDistributionPropertyFor(getBaseCriteria().getEntityClass(), aggregationProperty));
		}
	    }
	    aggregationProperties.addAll(aggregationsToAdd);
	    if (distributionProperties == null) {
		distributionProperties = new ArrayList<IDistributedProperty>();
	    }
	    distributionProperties.addAll(distributionsToAdd);

	}
	if (distributionProperties != null) {
	    for (final IDistributedProperty distributionProperty : distributionProperties) {
		aliases.put(distributionProperty, "pivot_report_alias_" + Integer.toString(aliasCounter++));
	    }
	}

    }

    public static IDistributedProperty createDistributionPropertyFor(final Class<?> clazz, final IAggregatedProperty aggregationProperty) {
	PropertyTypeDeterminator.determinePropertyType(clazz, aggregationProperty.getActualProperty());
	if (Date.class.isAssignableFrom(PropertyTypeDeterminator.determinePropertyType(clazz, aggregationProperty.getActualProperty()))) {
	    return new DistributionDateProperty(aggregationProperty.getName(), aggregationProperty.getDesc(), aggregationProperty.getActualProperty(), HqlDateFunctions.DAY);
	}
	return new DistributionProperty(aggregationProperty.getName(), aggregationProperty.getDesc(), aggregationProperty.getActualProperty());
    }

    public static IAggregatedProperty createAggregationPropertyFor(final IAggregatedProperty aggregationProperty, final AnalysisPropertyAggregationFunction aggregationFunction) {
	return new AggregationProperty(aggregationProperty.getName(), aggregationProperty.getDesc(), aggregationProperty.getActualProperty(), aggregationFunction);
    }

    public String getAliasFor(final IDistributedProperty property) {
	return aliases.get(property);
    }

    @Override
    public List<EntityAggregates> runExtendedQuery(final int pageSize) {
	final IQueryOrderedModel<EntityAggregates> queryModel = createExtendedQuery().model(EntityAggregates.class);
	return getQueryResult(queryModel, createExtendedFetchModel(), pageSize);
    }
}
