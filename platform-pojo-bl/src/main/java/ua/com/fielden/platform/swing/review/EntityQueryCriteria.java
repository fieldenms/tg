/**
 *
 */
package ua.com.fielden.platform.swing.review;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.IPropertyAggregationFunction;
import ua.com.fielden.platform.equery.PropertyAggregationFunction;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompleted;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompletedAndYielded;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.MiscUtilities;

import com.google.inject.Inject;

/**
 * Base class for every criteria for retrieval of {@link AbstractEntity}s. Each TG property of this class represents some query criterion.
 *
 * @author TG Team
 */
@KeyType(String.class)
public abstract class EntityQueryCriteria<T extends AbstractEntity, DAO extends IEntityDao<T>> extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    private final Map<String, IPropertyAggregationFunction> totals = new HashMap<String, IPropertyAggregationFunction>();
    private final Map<String, String> totalsAliases = new HashMap<String, String>();

    private final Map<String, IValueMatcher> valueMatchers = new HashMap<String, IValueMatcher>();
    private final IValueMatcherFactory valueMatcherFactory;

    private final DAO dao;
    private final IEntityAggregatesDao entityAggregatesDao;

    /**
     * Constructs {@link EntityQueryCriteria} instance with specified {@link IEntityAggregatesDao} and {@link IValueMatcherFactory}.
     * Needed mostly for instantiating through injector.
     *
     * @param entityAggregatesDao
     * @param valueMatcherFactory
     */
    @Inject
    protected EntityQueryCriteria(final IEntityAggregatesDao entityAggregatesDao, final IValueMatcherFactory valueMatcherFactory){
	this(null, entityAggregatesDao, valueMatcherFactory);
    }

    protected EntityQueryCriteria(final DAO dao, final IEntityAggregatesDao entityAggregatesDao, final IValueMatcherFactory valueMatcherFactory) {
	setKey("not required");
	this.dao = dao;
	this.entityAggregatesDao = entityAggregatesDao;
	this.valueMatcherFactory = valueMatcherFactory;
    }

    /**
     * Should create query without any ordering. Ordering is provided by TG properties annotated with {@link Ordered} annotation
     *
     * @return
     */
    protected abstract ICompleted createQuery();

    protected abstract fetch<T> createFetchModel();

    public Class<?> getQueryResultType() {
	return null;
    }

    /**
     * Should return query's first page of <code>pageSize</code>. Usually this method returns simply <code>dao.firstPage(query, pageSize)</code>.
     *
     * @param query
     * @param pageSize
     * @return
     */
    protected IPage<T> firstPage(final IQueryOrderedModel<T> query, final int pageSize) {
	return dao.firstPage(query, createFetchModel(), pageSize);
    }

    /**
     * Should return first page with specified size and filled with entities from query and summaryModel queries.
     *
     * @param query
     * @param summaryModel
     * @param pageSize
     * @return
     */
    protected IPage<T> firstPage(final IQueryOrderedModel<T> query, final IQueryOrderedModel<EntityAggregates> summaryModel, final int pageSize) {
	return dao.firstPage(query, createFetchModel(), summaryModel, pageSize);
    }

    /**
     * Should create query for lifecycle information.
     *
     * @return
     */
    protected IQueryModel<T> createLifecycleQueryBaseModel() {
	throw new UnsupportedOperationException("Should be used in DEQCriteria.");
    }

    /**
     * Should set values of this criteria to default.
     */
    protected void defaultValues() {
	for (final MetaProperty mp : Finder.getCollectionalMetaProperties(this, String.class)) {
	    set(mp.getName(), new ArrayList<String>());
	}
    }

    /**
     * Override this method to enable/disable "Default" button on EntityReview
     *
     * @return
     */
    public boolean isDefaultEnabled() {
	return true;
    }

    /**
     * Gets query from {@link #createQuery(int)} method, enhances it with ordering conditions, provided by {@link PropertiesOrderingModel#enhanceWithOrdering(ICompleted)} method
     * and calls {@link #firstPage(IQueryOrderedModel, int)} method with result.
     *
     * @param pageSize
     * @return
     */
    public final IPage<T> run(final int pageSize, final IOrderEnhancer orderEnhancer) {
	final ICompleted notOrderedQuery = createQuery();
	final IQueryOrderedModel<T> queryModel = orderEnhancer.enhanceWithOrdering(notOrderedQuery, getQueryResultType());
	return run(pageSize, queryModel);
    }

    public final IPage<T> run(final int pageSize) {
	final IQueryModel<T> notOrderedQuery = createQuery().model();
	return run(pageSize, notOrderedQuery);
    }

    private IPage<T> run(final int pageSize, final IQueryOrderedModel<T> queryModel) {
	if (isTotalsPresent()) {
	    return firstPage(queryModel, createQueryWithTotals(), pageSize);
	}
	return firstPage(queryModel, pageSize);
    }

    /**
     * Gets all entities.
     *
     * @param pageSize
     * @return
     */
    public final List<T> run(final IOrderEnhancer orderEnhancer) {
	final ICompleted notOrderedQuery = createQuery();
	final IQueryOrderedModel<T> queryModel = orderEnhancer.enhanceWithOrdering(notOrderedQuery, getQueryResultType());
	return dao.getEntities(queryModel, createFetchModel());
    }

    public String getAlias() {
	return null;
    }

    /**
     * Creates summary query.
     *
     * @return
     */
    protected IQueryModel<EntityAggregates> createQueryWithTotals() {
	int aliasCounter = 0;
	final ICompleted query = createQuery();
	totalsAliases.clear();
	ICompletedAndYielded queryYielded = query;
	for (final Entry<String, IPropertyAggregationFunction> entry : totals.entrySet()) {
	    final String currentAlias = "alias_" + aliasCounter++;
	    queryYielded = queryYielded.yieldExp(entry.getValue().createQueryString((!StringUtils.isEmpty(getAlias()) ? (getAlias() + ".") : "") + entry.getKey()), currentAlias);
	    totalsAliases.put(entry.getKey(), currentAlias);
	}
	return queryYielded.model(EntityAggregates.class);
    }

    /**
     * Returns value that indicates whether totals are present or not.
     *
     * @return
     */
    public boolean isTotalsPresent() {
	return totals.size() > 0;
    }

    public boolean isTotalsPresent(final String propertyName) {
	return totals.containsKey(isEmpty(propertyName) ? "id" : propertyName);
    }

    /**
     * Export the data based on the criteria query to the specified file.
     *
     * @param file
     * @param enhancer
     * @param propertyNames
     * @param propertyTitles
     * @throws IOException
     */
    public final void export(final File file, final IOrderEnhancer enhancer, final String[] propertyNames, final String[] propertyTitles) throws IOException {
	final ICompleted notOrderedQuery = createQuery();
	final IQueryOrderedModel<T> queryModel = enhancer.enhanceWithOrdering(notOrderedQuery, getQueryResultType());
	final byte[] content = dao.export(queryModel, createFetchModel(), propertyNames, propertyTitles);
	final FileOutputStream fo = new FileOutputStream(file);
	fo.write(content);
	fo.flush();
	fo.close();
    }

    public IValueMatcher<?> getValueMatcher(final String propertyName) {
	if (valueMatchers.get(propertyName) == null) {
	    valueMatchers.put(propertyName, valueMatcherFactory.getValueMatcher((Class<? extends AbstractEntity>) getType(), propertyName, this));
	}
	return valueMatchers.get(propertyName);
    }

    public EntityQueryCriteria<T, DAO> setValueMatcher(final String propertyName, final IValueMatcher<?> valueMatcher) {
	valueMatchers.put(propertyName, valueMatcher);
	return this;
    }

    /** A convenient method for converting a list of {@link PropertyDescriptor} to a list of strings. */
    public <T extends AbstractEntity> String[] preparePropertyDescriptors(final List<PropertyDescriptor<T>> criteria) {
	final List<String> result = new ArrayList<String>();
	for (final PropertyDescriptor<T> crit : criteria) {
	    if (crit != null) {
		result.add(crit.toString());
	    }
	}
	return result.toArray(new String[] {});
    }

    /**
     * Creates a new array of values based on the passed string by splitting criteria using comma and by changing * to %.
     *
     * @param criteria
     * @return
     */
    public static String[] prepare(final String criteria) {
	if (StringUtils.isEmpty(criteria)) {
	    return new String[] {};
	}

	final List<String> result = new ArrayList<String>();
	for (final String crit : criteria.split(",")) {
	    result.add(PojoValueMatcher.prepare(crit));
	}
	return result.toArray(new String[] {});
    }

    /**
     * Convenience method that returns only visible properties of this class
     *
     * @return
     */
    public List<MetaProperty> getVisibleProperties() {
	final List<MetaProperty> visibleMetaProperties = new ArrayList<MetaProperty>();
	for (final Entry<String, MetaProperty> metaPropertyEntry : getProperties().entrySet()) {
	    if (metaPropertyEntry.getValue().isVisible()) {
		visibleMetaProperties.add(metaPropertyEntry.getValue());
	    }
	}
	return visibleMetaProperties;
    }

    /**
     * After calling super method {@link #setMetaPropertyFactory(IMetaPropertyFactory)}, creates {@link PropertiesOrderingModel} for this instance
     */
    @Override
    protected void setMetaPropertyFactory(final IMetaPropertyFactory metaPropertyFactory) {
	super.setMetaPropertyFactory(metaPropertyFactory);

    }

    public DAO getDao() {
	return dao;
    }

    public IEntityAggregatesDao getEntityAggregatesDao() {
	return entityAggregatesDao;
    }

    /** Clones criteria. */
    @Override
    public EntityQueryCriteria<T, DAO> clone() {
	final EntityQueryCriteria<T, DAO> instance = (EntityQueryCriteria<T, DAO>) getEntityFactory().newByKey((Class<AbstractEntity>) getType(), "key");
	copyTo(instance);
	return instance;
    }

    /**
     * Makes copy of this criteria in to the specified one
     *
     * @param instance
     */
    public void copyTo(final EntityQueryCriteria<T, DAO> instance) {
	for (final MetaProperty prop : Finder.getMetaProperties(this)) {
	    if (List.class.isAssignableFrom(prop.getType())) {
		final List list = new ArrayList();
		list.addAll((List) this.get(prop.getName()));
		instance.set(prop.getName(), list);
	    } else {
		instance.set(prop.getName(), this.get(prop.getName()));
	    }
	}
    }

    public IValueMatcherFactory getValueMatcherFactory() {
	return valueMatcherFactory;
    }

    /**
     * Adds map of the property names and their associated aggregation functions to the list of totals.
     *
     * @param totals
     */
    public void addTotals(final Map<String, PropertyAggregationFunction> totals) {
	for (final Entry<String, PropertyAggregationFunction> totalPair : totals.entrySet()) {
	    addTotal(totalPair.getKey(), totalPair.getValue());
	}
    }

    /**
     * Adds property name and it's associated aggregation function to the list of totals.
     *
     * @param totals
     */
    public void addTotal(final String name, final IPropertyAggregationFunction function) {
	this.totals.put(name, function);
    }

    /**
     * Removes map of the property names and their associated aggregation functions from the totals.
     *
     * @param totals
     */
    public void removeTotals(final Map<String, PropertyAggregationFunction> totals) {
	for (final Entry<String, PropertyAggregationFunction> totalPair : totals.entrySet()) {
	    removeTotal(totalPair.getKey());
	}
    }

    /**
     * Removes total specified with property name from the totals.
     *
     * @param keyName
     * @return
     */
    public IPropertyAggregationFunction removeTotal(final String keyName) {
	return totals.remove(keyName);
    }

    /**
     * returns the list of totals.
     *
     * @return
     */
    public Map<String, IPropertyAggregationFunction> getTotals() {
	return Collections.unmodifiableMap(totals);
    }

    /**
     * TODO wright documentation.
     *
     * @param propertyName
     * @return
     */
    public IPropertyAggregationFunction getPropertyAggregationFunctionFor(final String propertyName) {
	return totals.get(propertyName);
    }

    /**
     * Clears all totals.
     */
    public void clearTotals() {
	totals.clear();
    }

    /**
     * Returns the alias for the specified property name
     *
     * @param propertyName
     * @return
     */
    public String getAliasForTotalsProperty(final String propertyName) {
	return totalsAliases.get(propertyName);
    }

    public static String[] prepare(final List<String> criteria) {
	return MiscUtilities.prepare(criteria);
    }

    /**
     * Returns the class that are used in the creation of query
     *
     * @return
     */
    public Class<T> getEntityClass() {
	return getDao().getEntityType();
    }
}
