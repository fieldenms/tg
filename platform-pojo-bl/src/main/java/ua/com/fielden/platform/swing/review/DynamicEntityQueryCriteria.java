package ua.com.fielden.platform.swing.review;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.IllegalClassException;

import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dynamictree.DynamicEntityTree;
import ua.com.fielden.platform.dynamictree.DynamicEntityTreeNode;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.equery.IParameter;
import ua.com.fielden.platform.equery.IParameterGetter;
import ua.com.fielden.platform.equery.IPropertyAggregationFunction;
import ua.com.fielden.platform.equery.IQueryModelProvider;
import ua.com.fielden.platform.equery.PropertyAggregationFunction;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.swing.review.persistens.DynamicCriteriaPersistentObject;
import ua.com.fielden.platform.treemodel.IPropertyFilter;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.PropertyChangeSupportEx.CheckingStrategy;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;


/**
 * This class allows to create criteria at runtime with defined list of properties. If one wants to add new property then the {@link #addProperty(List)} method must be used. In
 * order to add collection of properties, the one must use {@link #addProperties(Collection)} method. There are also defined appropriate methods for removing the properties from
 * the criteria ({@link #removeProperty(List)} and {@link #removeProperties(Collection)}).
 *
 * @author TG Team
 *
 */
@SuppressWarnings("unchecked")
public class DynamicEntityQueryCriteria<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends EntityQueryCriteria<T, DAO> {
    private static final long serialVersionUID = -3416480981984231794L;

    /**
     * Prefixes for the property names.
     */
    public static final String _IS = "_is", _NOT = "_not", _FROM = "_from", _TO = "_to";
    private final Map<String, DynamicProperty<T>> properties = new ListOrderedMap();

    /**
     * holds all selected criteria and resultant properties.
     */
    private final List<String> criteriaProperties = new ArrayList<String>();
    private final List<String> fetchProperties = new ArrayList<String>();

    /**
     * Dummy entity that holds the specified properties for synthetic entity.
     */
    private final AbstractEntity<?> dummyEntity;

    /**
     * Contains {@link AbstractUnionEntity} properties those must be excluded during querying.
     */
    private final List<String> excludeProperties = new ArrayList<String>();
    private final List<IPropertyListChangedListener> propertyListChangeHandlers = new ArrayList<IPropertyListChangedListener>();
    private final IDaoFactory daoFactory;
    private final EntityFactory entityFactory;

    private final IPropertyFilter criteriaFilter = new DefaultDynamicCriteriaPropertyFilter();

    //Needed for determining new file paths for the autocompleters.
    //private final String associatedKey;

    private final PropertiesNotFoundException restoreError;

    private final String ALIAS = "alias_for_main_criteria_type";

    @Override
    public String getAlias() {
	return ALIAS;
    }

    public DynamicEntityQueryCriteria(final EntityFactory entityFactory, final IDaoFactory daoFactory, final DAO dao, final IEntityAggregatesDao entityAggregatesDao, final IValueMatcherFactory valueMatcherFactory, final DynamicCriteriaPersistentObject persistentObject) {
	super(dao, entityAggregatesDao, valueMatcherFactory);

	this.entityFactory = entityFactory;
	this.daoFactory = daoFactory;

	if (IQueryModelProvider.class.isAssignableFrom(getEntityClass())) {
	    dummyEntity = entityFactory.newEntity(getEntityClass());
	} else {
	    dummyEntity = null;
	}

	if (persistentObject != null) {
	    final List<String> notFoundedCriteriaProperties = new ArrayList<String>();
	    for (final String property : persistentObject.getPersistentProperties()) {
		try {
		    final Pair<String, String> propNames = getPropertyNames(property);
		    final PropertyPersistentObject firstPersistedProperty = persistentObject.getCriteriaMappings().get(propNames.getKey());
		    final Pair<DynamicProperty<T>, DynamicProperty<T>> properties = createPropertyFrom(property);

		    if (firstPersistedProperty != null) {
			properties.getKey().setValue(firstPersistedProperty.getPropertyValue());
			properties.getKey().setNot(firstPersistedProperty.getNot());
			properties.getKey().setExclusive(firstPersistedProperty.getExclusive());
			properties.getKey().setDatePrefix(firstPersistedProperty.getDatePrefix());
			properties.getKey().setDateMnemonic(firstPersistedProperty.getDateMnemonic());
			properties.getKey().setAndBefore(firstPersistedProperty.getAndBefore());
			properties.getKey().setAll(firstPersistedProperty.getAll());
			properties.getKey().setOrNull(firstPersistedProperty.getOrNull());
		    }

		    if (properties.getValue() != null) {
			final PropertyPersistentObject secondPersistedProperty = persistentObject.getCriteriaMappings().get(propNames.getValue());
			if (secondPersistedProperty != null) {
			    properties.getValue().setValue(secondPersistedProperty.getPropertyValue());
			    properties.getValue().setNot(secondPersistedProperty.getNot());
			    properties.getValue().setExclusive(secondPersistedProperty.getExclusive());
			    properties.getValue().setDatePrefix(secondPersistedProperty.getDatePrefix());
			    properties.getValue().setDateMnemonic(secondPersistedProperty.getDateMnemonic());
			    properties.getValue().setAndBefore(secondPersistedProperty.getAndBefore());
			    properties.getValue().setAll(secondPersistedProperty.getAll());
			    properties.getValue().setOrNull(secondPersistedProperty.getOrNull());
			}
		    }
		} catch (final Exception ex) {
		    notFoundedCriteriaProperties.add(property);
		}
	    }
	    final List<String> notFoundedFetchProperties = new ArrayList<String>();
	    for (final String property : persistentObject.getTableHeaders()) {
		try {
		    addFetchProperty(property);
		} catch (final Exception ex) {
		    notFoundedFetchProperties.add(property);
		}
	    }
	    if (notFoundedCriteriaProperties.size() > 0 || notFoundedFetchProperties.size() > 0) {
		restoreError = new PropertiesNotFoundException(notFoundedCriteriaProperties, notFoundedFetchProperties);
	    } else {
		restoreError = null;
	    }

	} else {
	    restoreError = null;
	}

    }

    /**
     * Returns error that happened during restoring {@link DynamicEntityQueryCriteria} from {@link DynamicCriteriaPersistentObject} instnace.
     *
     * @return
     */
    public PropertiesNotFoundException getRestoreError() {
	return restoreError;
    }

    @Override
    public Class<?> getPropertyType(final String propertyName) {
	return getEditableProperty(propertyName).getType();
    }

    protected Map<String, DynamicProperty<T>> getPropertiesMap() {
	return Collections.unmodifiableMap(properties);
    }

    /**
     * Returns the dummy entity (see {@link #dummyEntity} for more information).
     *
     * @return
     */
    public final AbstractEntity<?> getDummyEntity() {
	return dummyEntity;
    }

    @Override
    public boolean isDefaultEnabled() {
	return properties.size() > 0;
    }

    /**
     * Determines whether property defined by "key" have empty value.
     *
     * @param key
     * @return
     */
    public boolean isEmptyValue(final String key) {
	final DynamicProperty<T> p = properties.get(key);
	return EntityUtils.equalsEx(p.getValue(), DynamicQueryBuilder.getEmptyValue(p.getType(), p.isSingle()));
    }

    public static final String DEFAULT = "DEFAULT_____________";

    /**
     * Sets a default value for concrete "key" of some {@link DynamicProperty}.
     *
     * @param key
     * @param property
     */
    protected void defaultValue(final String key) {
	final DynamicProperty<T> property = properties.get(key);
	set(key, DynamicQueryBuilder.getEmptyValue(property.getType(), property.isSingle()));

	getChangeSupport().firePropertyChange(DEFAULT + key, null, "Not null", CheckingStrategy.CHECK_EQUALITY, false);
    }

    @Override
    protected void defaultValues() {
	for (final String key : properties.keySet()) {
	    defaultValue(key);
	}
    }

    private Pair<DynamicProperty<T>, DynamicProperty<T>> createPropertyFrom(final String property) {
	final DynamicPropertyAnalyser propertyAnalyser = new DynamicPropertyAnalyser(getEntityClass(), property, getCriteriaFilter());
	if (!(propertyAnalyser.isPropertyVisible() && propertyAnalyser.isCriteriaPropertyAvailable())) {
	    throw new IllegalArgumentException("The " + property + " property can not be created");
	}
	final DynamicProperty<T> newProperty = new DynamicProperty<T>(this, property);
	criteriaProperties.add(property);
	final Pair<String, String> propertyNames = getPropertyNames(property);
	if (EntityUtils.isEntityType(newProperty.getType())) {
	    // newProperty.setDesc("filter by " + Character.toLowerCase(newProperty.getDesc().charAt(0)) + newProperty.getDesc().substring(1) + "...");
	    properties.put(propertyNames.getKey(), newProperty);
	    defaultValue(propertyNames.getKey());
	    firePropertyAdded(propertyNames.getKey(), newProperty, property);

	    return new Pair<DynamicProperty<T>, DynamicProperty<T>>(newProperty, null);
	} else if (EntityUtils.isString(newProperty.getType())) {
	    newProperty.setDesc("<html>filter by " + lowerFirstLetter(newProperty.getDesc()) + "...</html>");
	    properties.put(propertyNames.getKey(), newProperty);
	    defaultValue(propertyNames.getKey());
	    firePropertyAdded(propertyNames.getKey(), newProperty, property);

	    return new Pair<DynamicProperty<T>, DynamicProperty<T>>(newProperty, null);
	} else if (EntityUtils.isBoolean(newProperty.getType())) {

	    //Initiating additional property.
	    final DynamicProperty<T> additionalProperty = new DynamicProperty<T>(this, newProperty);
	    additionalProperty.setTitle("Is not " + lowerFirstLetter(additionalProperty.getTitle()));
	    additionalProperty.setDesc("<html>Is not " + lowerFirstLetter(additionalProperty.getDesc()) + "</html>");
	    properties.put(propertyNames.getValue(), additionalProperty);
	    defaultValue(propertyNames.getValue());
	    firePropertyAdded(propertyNames.getValue(), additionalProperty, property);

	    //Initiating main property.
	    newProperty.setTitle("Is " + lowerFirstLetter(newProperty.getTitle()));
	    newProperty.setDesc("<html>Is " + lowerFirstLetter(newProperty.getDesc()) + "</html>");
	    properties.put(propertyNames.getKey(), newProperty);
	    defaultValue(propertyNames.getKey());
	    firePropertyAdded(propertyNames.getKey(), newProperty, property);

	    return new Pair<DynamicProperty<T>, DynamicProperty<T>>(newProperty, additionalProperty);
	} else if (EntityUtils.isRangeType(newProperty.getType())) {
	    //Initiating additional property.
	    final DynamicProperty<T> additionalProperty = new DynamicProperty<T>(this, newProperty);
	    additionalProperty.setTitle(toDesc(additionalProperty.getTitle()));
	    additionalProperty.setDesc(toHint(lowerFirstLetter(additionalProperty.getDesc())));
	    properties.put(propertyNames.getValue(), additionalProperty);
	    defaultValue(propertyNames.getValue());
	    firePropertyAdded(propertyNames.getValue(), additionalProperty, property);

	    //Initiating main property.
	    newProperty.setTitle(fromDesc(newProperty.getTitle()));
	    newProperty.setDesc(fromHint(lowerFirstLetter(newProperty.getDesc())));
	    properties.put(propertyNames.getKey(), newProperty);
	    defaultValue(propertyNames.getKey());
	    firePropertyAdded(propertyNames.getKey(), newProperty, property);

	    return new Pair<DynamicProperty<T>, DynamicProperty<T>>(newProperty, additionalProperty);
	} else {
	    throw new IllegalClassException("DynamicEntityQueryCriteria doesn't support " + newProperty.getType() + " type of dynamic property");
	}
    }

    private String lowerFirstLetter(final String desc) {
	return Character.toLowerCase(desc.charAt(0)) + desc.substring(1);
    }

    private void firePropertyAdded(final String key, final DynamicProperty<T> property, final String propertyName) {
	for (final IPropertyListChangedListener handler : propertyListChangeHandlers) {
	    handler.propertyAdded(key, property, propertyName);
	}
    }

    private void firePropertyRemoved(final String key, final DynamicProperty<T> property, final String propertyName) {
	for (final IPropertyListChangedListener handler : propertyListChangeHandlers) {
	    handler.propertyRemoved(key, property, propertyName);
	}
    }

    /**
     * Adds aggregation functions associated with tree path.
     *
     * @param totals
     */
    public void setTotals(final Map<String, PropertyAggregationFunction> totals) {
	clearTotals();
	for (final Entry<String, PropertyAggregationFunction> entry : totals.entrySet()) {
	    final String propertyName = entry.getKey();
	    addTotal(isEmpty(propertyName) ? "id" : propertyName, entry.getValue());
	}
    }

    @Override
    public ICompleted createQuery() {
	if (checkForUnions()) {
	    throw new IllegalStateException("Criteira has union properperties. These properties arent't supported yet.");
	}
	final ICompleted result = DynamicQueryBuilder.buildConditions(createJoinCondition(), createQueryProperties(), getAlias());
	return result; //!IQueryModelProvider.class.isAssignableFrom(getEntityClass()) ? result : result.resultType(getEntityClass());
    }

    /**
     * Converts existing properties model (which has separate properties for from/to, is/isNot and so on)
     * into new properties model (which has single abstraction for one criterion).
     *
     * @param properties
     * @return
     */
    protected List<QueryProperty> createQueryProperties() {
	final List<QueryProperty> queryProperties = new ArrayList<QueryProperty>();
	for (final String actualProperty : criteriaProperties) {
	    queryProperties.add(createQueryProperty(actualProperty));
	}
	return queryProperties;
    }

    /**
     * Converts existing properties model (which has separate properties for from/to, is/isNot and so on)
     * into new properties model (which has single abstraction for one criterion).
     *
     * @param properties
     * @return
     */
    private QueryProperty createQueryProperty(final String actualProperty) {
	final Pair<String, String> names = getPropertyNames(actualProperty);
	final DynamicProperty<T> mainProperty = getEditableProperty(names.getKey());
	final DynamicProperty<T> additionalProperty = getEditableProperty(names.getValue());

	final QueryProperty queryProperty = new QueryProperty(getEntityClass(), actualProperty, getAlias());

	queryProperty.setValue(mainProperty.getValue());
	queryProperty.setExclusive(mainProperty.getExclusive());
	if (additionalProperty != null) {
	    queryProperty.setValue2(additionalProperty.getValue());
	    queryProperty.setExclusive2(additionalProperty.getExclusive());
	}

	queryProperty.setDatePrefix(mainProperty.getDatePrefix());
	queryProperty.setDateMnemonic(mainProperty.getDateMnemonic());
	queryProperty.setAndBefore(mainProperty.getAndBefore());
	queryProperty.setOrNull(mainProperty.getOrNull());
	queryProperty.setNot(mainProperty.getNot());
	queryProperty.setAll(mainProperty.getAll());
	return queryProperty;
    }

    /**
     * Checks whether criteria or fetch properties contains the union properties.
     *
     * @return
     */
    private boolean checkForUnions() {
	return !excludeProperties.isEmpty() || isListContainsUnionProperty(criteriaProperties) || isListContainsUnionProperty(fetchProperties);
    }

    /**
     * Determines whether specified list contains union properties.
     *
     * @param properties
     * @return
     */
    private boolean isListContainsUnionProperty(final List<String> properties) {
	final DynamicPropertyAnalyser propertyAnalyser = new DynamicPropertyAnalyser(getCriteriaFilter());
	for (final String property : properties) {
	    propertyAnalyser.setAnalyseProperty(getEntityClass(), property);
	    if (propertyAnalyser.isAbstractUnionProperty()) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public fetch<T> createFetchModel() {
	try {
	    final DynamicEntityTree<T> fetchTree = new DynamicEntityTree<T>(getFetchProperties(), getEntityClass());
	    final fetch<T> main = buildFetchModels(getEntityClass(), fetchTree.getRoot());
	    return main;
	} catch (final Exception e1) {
	    throw new RuntimeException(e1);
	}
    }

    //    private Pair<List<String>, List<List<String>>> devideCriteiraProperties(final String prefix) {
    //	final List<String> simpleProperties = new ArrayList<String>();
    //	final List<List<String>> polymorphicProperties = new ArrayList<List<String>>();
    //	final List<String> uncheckedProperties = new ArrayList<String>(getCriteriaProperties());
    //	return new Pair<List<String>, List<List<String>>>(simpleProperties, polymorphicProperties);
    //    }

    protected IJoin createJoinCondition() {
	try {
	    if (IQueryModelProvider.class.isAssignableFrom(getEntityClass())) { // synthetic entity case
		return select(((IQueryModelProvider) getDummyEntity()).model(createParameterGetter())).as(getAlias());
	    } else {
		return select(getEntityClass()).as(getAlias());
	    }
	} catch (final Exception e) {
	    throw new RuntimeException("Can not create join condition due to: " + e + ".");
	}
    }

    /**
     * Returns parameter getter for conventional property names. (without "_from", "_to" etc. suffixes and class prefixes)
     *
     * @return
     */
    public final IParameterGetter createParameterGetter() {
	return new IParameterGetter() {
	    @Override
	    public IParameter getParameter(final String propertyName) {
		final Pair<String, String> rangePropertyNames = getPropertyNames(propertyName);
		if (!properties.containsKey(rangePropertyNames.getKey())) {
		    return null;
		}
		final QueryProperty queryProperty = createQueryProperty(propertyName);
		return new IParameter() {
		    @Override
		    public boolean isRange() {
			return queryProperty.isCritOnly() ? !queryProperty.isSingle() : EntityUtils.isRangeType(queryProperty.getType());
		    }

		    @Override
		    public Object getValue() throws UnsupportedOperationException {
			if (isRange()) {
			    throw new UnsupportedOperationException("Single value that corresponds to " + propertyName + " property could not be determined from range property.");
			}
			return queryProperty.getValue();
		    }

		    @Override
		    public Pair<Object, Object> getRange() throws UnsupportedOperationException {
			if (!isRange()) {
			    throw new UnsupportedOperationException("Range value that corresponds to " + propertyName + " property could not be determined from single property.");
			}
			if (EntityUtils.isDate(queryProperty.getType()) && queryProperty.getDatePrefix() != null && queryProperty.getDateMnemonic() != null) {
			    // left boundary should be inclusive and right -- exclusive!
			    final Pair<Date, Date> fromAndTo = DynamicQueryBuilder.getDateValuesFrom(queryProperty.getDatePrefix(), queryProperty.getDateMnemonic(), queryProperty.getAndBefore());
			    return new Pair<Object, Object>(fromAndTo.getKey(), fromAndTo.getValue());
			}
			return new Pair<Object, Object>(queryProperty.getValue(), queryProperty.getValue2());
		    }
		};
//		return new IParameter() {
//		    @Override
//		    public boolean isRange() {
//			final DynamicPropertyAnalyser propertyAnalyser = new DynamicPropertyAnalyser(getEntityClass(), propertyName, getCriteriaFilter());
//			final CritOnly critOnly = propertyAnalyser.getPropertyFieldAnnotation(CritOnly.class);
//			return critOnly == null ? (rangePropertyNames.getValue() != null) : !Type.SINGLE.equals(critOnly.value());
//		    }
//
//		    @Override
//		    public Object getValue() throws UnsupportedOperationException {
//			if (isRange()) {
//			    throw new UnsupportedOperationException("Single value that corresponds to " + propertyName + " property could not be determined from range property.");
//			}
//			return get(rangePropertyNames.getKey()); // FROM criteria is used for "range" properties with single selection (annotated with @SelectBy)
//		    }
//
//		    @Override
//		    public Pair<Object, Object> getRange() throws UnsupportedOperationException {
//			if (!isRange()) {
//			    throw new UnsupportedOperationException("Range value that corresponds to " + propertyName + " property could not be determined from single property.");
//			}
//			return new Pair<Object, Object>(get(rangePropertyNames.getKey()), get(rangePropertyNames.getValue()));
//		    }
//		};
	    }
	};
    }

    /**
     *
     * @param query
     * @param treeNode
     * @param previousType
     * @param aliasCounter
     * @param alias
     * @return
     * @throws Exception
     */
    private fetch<T> buildFetchModels(final Class<T> entityType, final DynamicEntityTreeNode treeNode) throws Exception {
	fetch fetchModel = fetch(entityType);

	if (treeNode == null || treeNode.getChildCount() == 0) {
	    return fetchModel;
	}

	for (final DynamicEntityTreeNode dynamicTreeNode : treeNode.getChildren()) {
	    final Class<?> propertyType = dynamicTreeNode.getType();
	    if (/* isKey(childField) || */!EntityUtils.isEntityType(propertyType)) {
		continue;
	    }
	    // commented to prevent the logic for determining polymorphic entities -- this logic will need to be changed in the future.
	    // final List<Class<?>> classes = Reflector.getAllNonAbstractClassesDerivedFrom(path, entityPackage, childField.getType());
	    // if (classes.size() == 0) {
	    // classes.add(childField.getType());
	    // }
	    // for (final Class<?> clazz : classes) {
	    final fetch<T> fetchSubModel = buildFetchModels((Class<T>) propertyType, dynamicTreeNode);
	    // }
	    fetchModel = fetchModel.with(dynamicTreeNode.getName(), fetchSubModel);
	}

	return fetchModel;
    }

    @Override
    public Object get(final String propertyName) {
	return properties.get(propertyName).getValue();
    }

    @Override
    public void set(final String propertyName, final Object value) {
	final DynamicProperty<T> property = properties.get(propertyName);
	if (property == null) {
	    return;
	}
	final Object oldValue = property.getValue();
	property.setValue(value);
	getChangeSupport().firePropertyChange(propertyName, oldValue, value, CheckingStrategy.CHECK_EQUALITY, false);
    }

    @Override
    public DynamicEntityQueryCriteria<T, DAO> clone() {
	final DynamicEntityQueryCriteria<T, DAO> dynamicQuery = new DynamicEntityQueryCriteria<T, DAO>(getCriteriaEntityFactory(), getDaoFactory(), getDao(), getEntityAggregatesDao(), getValueMatcherFactory(), null);
	copyTo(dynamicQuery);
	return dynamicQuery;
    }

    public IDaoFactory getDaoFactory() {
	return daoFactory;
    }

    public EntityFactory getCriteriaEntityFactory() {
	return entityFactory;
    }

    //    public IEntityDao<? extends AbstractEntity> getDaoForProperty(final String property) {
    //	final DynamicProperty dynamicProperty = getEditableProperty(property);
    //	if (getDaoFactory() == null || dynamicProperty == null || AbstractEntity.class.isAssignableFrom(dynamicProperty.getType())) {
    //	    return null;
    //	}
    //	return getDaoFactory().newDao(dynamicProperty.getType());
    //    }

    @Override
    public void copyTo(final EntityQueryCriteria<T, DAO> instance) {
	if (!(instance instanceof DynamicEntityQueryCriteria)) {
	    super.copyTo(instance);
	    return;
	}
	final DynamicEntityQueryCriteria<T, DAO> criteria = (DynamicEntityQueryCriteria<T, DAO>) instance;
	criteria.criteriaProperties.clear();
	criteria.criteriaProperties.addAll(getCriteriaProperties());
	criteria.fetchProperties.clear();
	criteria.fetchProperties.addAll(getFetchProperties());
	for (final String key : properties.keySet()) {
	    final DynamicProperty<T> property = properties.get(key);
	    if (property == null) {
		continue;
	    }
	    criteria.properties.put(key, property.copy(criteria, property));
	}
	for (final Entry<String, IPropertyAggregationFunction> totalsEntry : getTotals().entrySet()) {
	    criteria.addTotal(totalsEntry.getKey(), totalsEntry.getValue());
	}
    }

    public DynamicProperty<T> getEditableProperty(final String key) {
	return key == null ? null : properties.get(key);
    }

    /**
     * Should return sorted set, because {@link #properties} map is instance of {@link ListOrderedMap}.
     *
     * @return
     */
    public Set<String> getKeySet() {
	return properties.keySet();
    }

    /**
     * Removes previously selected dynamic properties and adds specified properties.
     *
     * @param properties
     */
    public void setProperties(final Collection<String> properties) {
	this.properties.clear();
	this.criteriaProperties.clear();
	addProperties(properties);
    }

    /**
     * Adds new dynamic criteria properties.
     *
     * @param properties
     */
    public void addProperties(final Collection<String> properties) {
	for (final String property : properties) {
	    addProperty(property);
	}
    }

    /**
     * Adds new dynamic criteria property.
     *
     * @param newProperty
     */
    public void addProperty(final String newProperty) {
	if (!criteriaProperties.contains(newProperty)) {
	    createPropertyFrom(newProperty);
	}
    }

    /**
     * Removes specified dynamic criteria property.
     *
     * @param propertyName
     */
    public void removeProperty(final String propertyName) {
	removeFromMap(propertyName);
    }

    /**
     * Removes specified dynamic criteria properties.
     *
     * @param properties
     */
    public void removeProperties(final Collection<String> properties) {
	for (final String property : properties) {
	    removeProperty(property);
	}
    }

    /**
     * Determines whether specified property is in the list of dynamic criteria properties.
     *
     * @param property
     * @return
     */
    public boolean isCriteriaPropertySelected(final String property) {
	return criteriaProperties.contains(property);
    }

    /**
     * Returns the list of excluded {@link AbstractUnionEntity} properties. See {@link #excludeProperties} fore more information.
     *
     * @return
     */
    public List<String> getExcludeProperties() {
	return Collections.unmodifiableList(excludeProperties);
    }

    /**
     * Set the properties those mustn't be included in the query for AbstractUnionEntity.
     *
     * @param unionProperties
     */
    public void setExcludeProperties(final List<String> excludeProeprties) {
	this.excludeProperties.clear();
	this.excludeProperties.addAll(excludeProeprties);
    }

    /**
     * Adds {@link AbstractUnionEntity} properties those must be excluded during querying.
     *
     * @param excludeProeprties
     */
    public void addExcludeProperties(final List<String> excludeProeprties) {
	for (final String property : excludeProeprties) {
	    addExcludeProperty(property);
	}
    }

    /**
     * Adds {@link AbstractUnionEntity} property that must be excluded during querying.
     *
     * @param excludeProeprty
     */
    public void addExcludeProperty(final String excludeProperty) {
	final DynamicCriteriaPropertyAnalyser analyser = new DynamicCriteriaPropertyAnalyser(getEntityClass(), excludeProperty, getCriteriaFilter());
	if (analyser.isPropertyUnion() && analyser.isPropertyVisible()) {
	    excludeProperties.add(excludeProperty);
	} else {
	    throw new IllegalArgumentException("The " + excludeProperty + " property can not be excluded");
	}
    }

    /**
     * Removes specified property from the list of excluded properties. See {@link #excludeProperties} for more information about the list of excluded properties.
     *
     * @param excludeProperty
     */
    public void removeExcludeProperty(final String property) {
	excludeProperties.remove(property);
    }

    /**
     * See {@link #removeExcludeProperty(String)} for more information
     *
     * @param excludeProperties
     */
    public void removeExcludeProperties(final List<String> properties) {
	for (final String property : properties) {
	    removeExcludeProperty(property);
	}
    }

    /**
     * Determines whether specified property should be excluded during querying or not.
     *
     * @param property
     * @return
     */
    public boolean isPropertyExcluded(final String property) {
	return excludeProperties.contains(property);
    }

    /**
     * Returns the number of {@link AbstractUnionEntity} properties those must be excluded during querying.
     *
     * @return
     */
    public int getExcludePropertyCount() {
	return excludeProperties.size();
    }

    /**
     * Adds properties those must be fetched with data.
     *
     * @param properties
     */
    public void addFetchProperties(final Collection<String> properties) {
	for (final String property : properties) {
	    addFetchProperty(property);
	}
    }

    /**
     * Should be overridden by descendants in order to provide custom property filter.
     *
     * @return
     */
    public IPropertyFilter getCriteriaFilter() {
	return criteriaFilter;
    }

    /**
     * Adds property that must be fetched with data.
     *
     * @param property
     */
    public void addFetchProperty(final String property) {
	if (!fetchProperties.contains(property)) {
	    final DynamicCriteriaPropertyAnalyser propertyAnalyser = new DynamicCriteriaPropertyAnalyser(getEntityClass(), property, getCriteriaFilter());
	    if (!(propertyAnalyser.isPropertyVisible() && propertyAnalyser.isFetchPropertyAvailable())) {
		throw new IllegalArgumentException("Fetch property for " + property + " can not be created.");
	    }
	    fetchProperties.add(property);
	}
    }

    /**
     * Removes fetch properties. See {@link #addFetchProperty(String)}.
     *
     * @param properties
     */
    public void removeFetchProperties(final Collection<String> properties) {
	for (final String property : properties) {
	    removeFetchProperty(property);
	}
    }

    /**
     * Removes fetch property. See {@link #addFetchProperty(String)}.
     *
     * @param property
     */
    public void removeFetchProperty(final String property) {
	fetchProperties.remove(property);
    }

    /**
     * Removes previous fetch properties. And adds new specified fetch properties.
     *
     * @param properties
     */
    public void setFetchProperties(final Collection<String> properties) {
	fetchProperties.clear();
	addFetchProperties(properties);
    }

    /**
     * Determines whether specified property is already in the list of fetch properties or not.
     *
     * @param property
     * @return
     */
    public boolean isFetchPropertySelected(final String property) {
	return fetchProperties.contains(property);
    }

    /**
     * Returns unmodifiable fetch properties.
     *
     * @return
     */
    public List<String> getFetchProperties() {
	return Collections.unmodifiableList(fetchProperties);
    }

    /**
     * Returns unmodifiable criteria properties.
     *
     * @return
     */
    public List<String> getCriteriaProperties() {
	return Collections.unmodifiableList(criteriaProperties);
    }

    @Override
    protected Result validate() {
	if (dummyEntity != null) {
	    dummyEntity.isValid();
	}
	for (final DynamicProperty<T> dynamicProperty : properties.values()) {
	    if (dynamicProperty.isSingle()) {
		if (!dummyEntity.getProperty(dynamicProperty.getActualPropertyName()).isValid()) {
		    return new Result(this, new Exception("Please correct all invalid values first!"));
		}
	    }
	}
	return Result.successful(this);
    }

    /**
     * Returns pair of identical names for the propertyName if the type of the property specified with propertyName is {@link Number}, boolean or {@link Date}. Otherwise it returns
     * only one name.
     *
     * @param propertyName
     * @return
     */
    public Pair<String, String> getPropertyNames(final String propertyName) {
	final DynamicPropertyAnalyser analyser = new DynamicPropertyAnalyser(getEntityClass(), propertyName, getCriteriaFilter());
	final String fullName = analyser.getCriteriaFullName();
	final Class<?> propertyType = analyser.getPropertyType();
	final String propertyNamePrefix = getEntityClass().getSimpleName();
	if (EntityUtils.isEntityType(propertyType) || EntityUtils.isString(propertyType)) {
	    return new Pair<String, String>(propertyNamePrefix + "." + fullName, null);
	} else if (EntityUtils.isBoolean(propertyType)) {
	    return new Pair<String, String>(propertyNamePrefix + "." + fullName + _IS, propertyNamePrefix + "." + fullName + _NOT);
	} else if (EntityUtils.isRangeType(propertyType)) {
	    return new Pair<String, String>(propertyNamePrefix + "." + fullName + _FROM, propertyNamePrefix + "." + fullName + _TO);
	} else {
	    throw new IllegalClassException("DynamicEntityQueryCriteria doesn't support " + propertyType + " type of dynamic property");
	}
    }

    public void addPropertyListChangeListener(final IPropertyListChangedListener propertyListChangeHandler) {
	propertyListChangeHandlers.add(propertyListChangeHandler);
    }

    public void removePropertyListChangedListener(final IPropertyListChangedListener propertyListChangeHandler) {
	propertyListChangeHandlers.remove(propertyListChangeHandler);
    }

    private void removeFromMap(final String propertyToRemove) {
	final DynamicPropertyAnalyser propertyAnalyser = new DynamicPropertyAnalyser(getEntityClass(), propertyToRemove, getCriteriaFilter());
	if (!propertyAnalyser.canRemoveCriteraProperty()) {
	    throw new IllegalStateException("The " + propertyToRemove + " from " + getEntityClass().getName() + " class can not be removed");
	}
	if (criteriaProperties.remove(propertyToRemove)) {
	    final Pair<String, String> propertyNames = getPropertyNames(propertyToRemove);
	    final DynamicProperty<T> property = properties.remove(propertyNames.getKey());
	    firePropertyRemoved(propertyNames.getKey(), property, propertyToRemove);
	    if (propertyNames.getValue() != null) {
		final DynamicProperty<T> additionalProperty = properties.remove(propertyNames.getValue());
		firePropertyRemoved(propertyNames.getValue(), additionalProperty, propertyToRemove);
	    }
	}
    }

    public IPropertyListChangedListener[] getPropertyListChangedListeners() {
	return propertyListChangeHandlers.toArray(new IPropertyListChangedListener[propertyListChangeHandlers.size()]);
    }

    /**
     * Composes ''from'' for PredefinedPropertyModel criteria.
     *
     * @param filteredPropertyDesc
     * @return
     */
    private static String fromDesc(final String filteredPropertyDesc) {
	return filteredPropertyDesc + " from";
    }

    /**
     * Composes ''to'' for PredefinedPropertyModel criteria.
     *
     * @param filteredPropertyDesc
     * @return
     */
    private static String toDesc(final String filteredPropertyDesc) {
	return filteredPropertyDesc + " to";
    }

    /**
     * Composes ''from'' for PredefinedPropertyModel criteria hint.
     *
     * @param filteredPropertyDesc
     * @return
     */
    private static String fromHint(final String filteredPropertyDesc) {
	return "<html>filter by " + filteredPropertyDesc + " from" + " ...</html>";
    }

    /**
     * Composes ''to'' for PredefinedPropertyModel criteria hint.
     *
     * @param filteredPropertyDesc
     * @return
     */
    private static String toHint(final String filteredPropertyDesc) {
	return "<html>filter by " + filteredPropertyDesc + " to" + " ...</html>";
    }
}