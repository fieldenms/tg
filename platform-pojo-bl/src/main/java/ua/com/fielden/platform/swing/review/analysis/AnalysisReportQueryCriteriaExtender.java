package ua.com.fielden.platform.swing.review.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SortOrder;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompleted;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompletedAndOrdered;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompletedAndYielded;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.review.EntityQueryCriteriaExtender;

public class AnalysisReportQueryCriteriaExtender<T extends AbstractEntity, DAO extends IEntityDao<T>> extends EntityQueryCriteriaExtender<T, DAO, IPage<EntityAggregates>> {

    private final Map<IDistributedProperty, String> aliases = new HashMap<IDistributedProperty, String>();

    private IDistributedProperty distributionProperty;
    private List<IAggregatedProperty> aggregationProperties;
    private IAggregatedProperty sortingProperty;
    private SortOrder sortOrder;

    public IDistributedProperty getDistributionProperty() {
	return distributionProperty;
    }

    public void setDistributionProperty(final IDistributedProperty distributionProperty) {
	this.distributionProperty = distributionProperty;
    }

    public List<IAggregatedProperty> getAggregationProperties() {
	return Collections.unmodifiableList(aggregationProperties);
    }

    public void setAggregationProperties(final List<IAggregatedProperty> aggregationProperties) {
	this.aggregationProperties = aggregationProperties;
    }

    public IAggregatedProperty getSortingProperty() {
	return sortingProperty;
    }

    public void setSortingProperty(final IAggregatedProperty sortingProperty) {
	this.sortingProperty = sortingProperty;
    }

    public SortOrder getSortOrder() {
	return sortOrder;
    }

    public void setSortOrder(final SortOrder sortOrder) {
	this.sortOrder = sortOrder;
    }

    protected ICompletedAndOrdered createExtendedQuery() {
	initAliases();
	if (aliases.size() == 0 || getDistributionProperty() == null) {
	    return null;
	} else {
	    final IDistributedProperty distributionProperty = getDistributionProperty();
	    distributionProperty.setTableAlias(getBaseCriteria().getAlias());
	    final String groupParsedParameter = getDistributionProperty().getParsedValue();
	    if (StringUtils.isEmpty(groupParsedParameter)) {
		return null;
	    }
	    final ICompleted queryBase = distributionProperty.isExpression() ? getBaseQueryModel().groupByExp(groupParsedParameter)
		    : getBaseQueryModel().groupByProp(groupParsedParameter);

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

	    String groupParameter = null;
	    Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(getBaseCriteria().getEntityClass(), getDistributionProperty().getActualProperty());
	    if (AbstractEntity.class.isAssignableFrom(propertyType)) {
		groupParameter = getDistributionProperty().getActualProperty();
		while (AbstractEntity.class.isAssignableFrom(propertyType)) {
		    groupParameter += ".key";
		    propertyType = PropertyTypeDeterminator.determinePropertyType(getBaseCriteria().getEntityClass(), groupParameter);
		}
	    } else {
		groupParameter = getAliasForDistributionProperty();
	    }
	    if (getSortingProperty() == null || getSortOrder() == SortOrder.UNSORTED || StringUtils.isEmpty(getSortingPropertyAlias())) {
		final String orderQueryPrefix = StringUtils.isEmpty(getBaseCriteria().getAlias()) ? "" : getBaseCriteria().getAlias() + ".";
		return queryFinalBase.orderBy(orderQueryPrefix + groupParameter);
	    } else {
		return queryFinalBase.orderBy(getSortingPropertyAlias() + " " + (getSortOrder() == SortOrder.ASCENDING ? "asc" : "desc"));
	    }
	}
    }

    protected fetch<EntityAggregates> createExtendedFetchModel() {
	final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(getBaseCriteria().getEntityClass(), getDistributionProperty().getActualProperty());
	if (AbstractEntity.class.isAssignableFrom(propertyType)) {
	    return new fetch<EntityAggregates>(EntityAggregates.class).with(getAliasForDistributionProperty(), new fetch(propertyType));
	} else {
	    return new fetch<EntityAggregates>(EntityAggregates.class);
	}
    }

    protected IPage<EntityAggregates> getQueryResult(final IQueryOrderedModel<EntityAggregates> queryModel, final fetch<EntityAggregates> fetchModel, final int pageSize) {
	return getBaseCriteria().getEntityAggregatesDao().firstPage(queryModel, fetchModel, pageSize);
    }

    private void initAliases() {
	int aliasCounter = 0;
	aliases.clear();
	for (int aggregationCounter = 0; aggregationCounter < aggregationProperties.size(); aggregationCounter++) {
	    final IAggregatedProperty aggregationProperty = aggregationProperties.get(aggregationCounter);
	    aliases.put(aggregationProperty, "analysis_report_alias_" + Integer.toString(aliasCounter++));
	}
	if (distributionProperty != null) {
	    aliases.put(distributionProperty, "analysis_report_alias_" + Integer.toString(aliasCounter++));
	}
    }

    public String getAliasFor(final IDistributedProperty property) {
	return aliases.get(property);
    }

    public String getAliasForDistributionProperty() {
	return aliases.get(getDistributionProperty());
    }

    public String getAliasForAggregationProperty(final int index) {
	return aliases.get(aggregationProperties.get(index));
    }

    public String getSortingPropertyAlias() {
	return aliases.get(getSortingProperty());
    }

    @Override
    public IPage<EntityAggregates> runExtendedQuery(final int pageSize) {
	final IQueryOrderedModel<EntityAggregates> queryModel = createExtendedQuery().model(EntityAggregates.class);
	return getQueryResult(queryModel, createExtendedFetchModel(), pageSize);
    }

}
