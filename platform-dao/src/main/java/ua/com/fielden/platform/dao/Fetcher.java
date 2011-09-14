package ua.com.fielden.platform.dao;

import static ua.com.fielden.platform.equery.equery.select;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.equery.ColumnInfo;
import ua.com.fielden.platform.equery.ColumnInfoForEntityProp;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.IQueryModelProvider;
import ua.com.fielden.platform.equery.ModelResult;
import ua.com.fielden.platform.equery.ReturnedModelResult;
import ua.com.fielden.platform.equery.ReturnedModelResult.TypesInfo;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompleted;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.persistence.types.EnumUserType;
import ua.com.fielden.platform.persistence.types.MoneyUserType;
import ua.com.fielden.platform.persistence.types.MoneyWithTaxAmountUserType;
import ua.com.fielden.platform.persistence.types.PropertyDescriptorType;
import ua.com.fielden.platform.persistence.types.SecurityTokenType;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.persistence.types.SimplyMoneyWithTaxAmountType;
import ua.com.fielden.platform.persistence.types.SimplyMoneyWithTaxAndExTaxAmountType;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * This class contains the Hibernate driven implementation of getting results from the provided IQueryOrderedModel.
 *
 * @author TG team
 *
 * @param <E>
 */
class Fetcher<E extends AbstractEntity> {
    private final static String ID_PROPERTY_NAME = "id";
    private Session session;
    private MappingExtractor mappingExtractor;
    private EntityFactory entityFactory;
    private Logger logger = Logger.getLogger(this.getClass());

    protected Fetcher() {
    }

    private Fetcher(final Session session, final MappingExtractor mappingExtractor, final EntityFactory entityFactory) {
	this.session = session;
	this.mappingExtractor = mappingExtractor;
	this.entityFactory = entityFactory;
    }

    /**
     * Executes the Hibernate query derived from the provided equery model "AS IS" (without fetching via models) and returns the results of the specified page.
     *
     * @param query
     * @param pageNumber
     * @param pageCapacity
     * @return
     */
    @SessionRequired
    private List<EntityContainer<E>> listAsIs(final ReturnedModelResult modelResult, final Integer pageNumber, final Integer pageCapacity) {
	final Map<String, TypesInfo> aliases = modelResult.getScalarAliases();

	final Query query = produceHibernateQuery(modelResult.getSql(), aliases, modelResult.getParamValues());
	logger.info("query:\n   " + query.getQueryString() + "\n");
	if (pageNumber != null && pageCapacity != null) {
	    query.setFirstResult(pageNumber * pageCapacity)//
	    .setFetchSize(pageCapacity)//
	    .setMaxResults(pageCapacity);
	}

	final DateTime st = new DateTime();
	@SuppressWarnings("unchecked")
	final List<EntityContainer<E>> list = transformFromNativeResult(aliases, modelResult, query.list());
	final Period pd = new Period(st, new DateTime());
	logger.info("Duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms. Entities count: " + list.size());

	return list;
    }

    /**
     * A helper method to create and set parameters values for Hibernate Query based on the provided instance of {@link IQueryOrderedModel}.
     *
     * @param query
     * @param hql
     * @return
     */
    private Query produceHibernateQuery(final String sql, final Map<String, TypesInfo> aliases, final Map<String, Object> queryParams) {
	final SQLQuery q = session.createSQLQuery(sql);

	for (final Map.Entry<String, TypesInfo> aliasEntry : aliases.entrySet()) {
	    if (aliasEntry.getValue() != null && isHibernateType(aliasEntry.getValue().getHibType())) {
		org.hibernate.type.Type hibernateType = null;
		try {
		    hibernateType = (org.hibernate.type.Type) aliasEntry.getValue().getHibType().newInstance();
		} catch (final Exception e) {
		    throw new RuntimeException("Couldn't obtain hibernate type: " + e);
		}
		logger.info("adding scalar: alias = [" + aliasEntry.getKey() + "] type = [" + hibernateType.getClass().getName() + "]");
		q.addScalar(aliasEntry.getKey(), hibernateType);
	    } else {
		logger.info("adding scalar: alias = [" + aliasEntry.getKey() + "]");
		q.addScalar(aliasEntry.getKey());
	    }
	}

	for (final Map.Entry<String, Object> paramEntry : queryParams.entrySet()) {
	    logger.info("about to set param: name = [" + paramEntry.getKey() + "] value = [" + paramEntry.getValue() + "]");
	    if (paramEntry.getValue() instanceof Collection) {
		q.setParameterList(paramEntry.getKey(), (Collection) paramEntry.getValue());
	    } else {
		q.setParameter(paramEntry.getKey(), paramEntry.getValue());
	    }
	    logger.debug("setting param: name = [" + paramEntry.getKey() + "] value = [" + paramEntry.getValue() + "]");
	}

	return q;
    }

    private List<EntityContainer<E>> transformFromNativeResult(final Map<String, TypesInfo> aliases, final ModelResult modelResult, final List<Object> nativeResult) {
	final List<EntityContainer<E>> result = new ArrayList<EntityContainer<E>>();
	for (final Object nativeEntry : nativeResult) {
	    final Object[] nativeEntries = nativeEntry instanceof Object[] ? (Object[]) nativeEntry : new Object[] { nativeEntry };
	    final EntityContainer<E> entity = transformTuple(modelResult.getResultType(), modelResult.getPrimitivePropsAliases(), modelResult.getEntityPropsMappers(), getMap(nativeEntries, aliases), true);
	    result.add(entity);
	}
	return result;
    }

    private Map<String, NativeData> getMap(final Object[] tuple, final Map<String, TypesInfo> aliases) {
	final Map<String, NativeData> result = new HashMap<String, NativeData>();
	int index = 0;
	for (final Map.Entry<String, TypesInfo> entry : aliases.entrySet()) {
	    result.put(entry.getKey(), new NativeData(tuple[index], entry.getValue()));
	    index = index + 1;
	}
	return result;
    }

    private Object convertFromHibernateUserType(final UserType hibernateUserType, final Object value) {
	if (value == null) {
	    return value;
	} else if (EnumUserType.class.equals(hibernateUserType)) {
	    return Enum.valueOf(((EnumUserType) hibernateUserType).returnedClass(), (String) value);
	} else if (hibernateUserType instanceof SecurityTokenType) {
	    try {
		return Class.forName((String) value);
	    } catch (final ClassNotFoundException e) {
		throw new HibernateException("Security token for value '" + value + "' could not be found");
	    }
	} else if (hibernateUserType instanceof PropertyDescriptorType) {
	    try {
		return PropertyDescriptor.fromString((String) value, entityFactory); // doStuff(
	    } catch (final Exception e) {
		throw new RuntimeException(e);
	    }
	}

	return null;
    }

    private Object convertFromHibernateCompositeUserType(final CompositeUserType hibernateUserType, final List<Object> values) {
	if (values == null || (values != null && values.size() == 0) || values.get(values.size() - 1) == null) { // the last check is equivalent to resultSet.wasNull() check in xxx.nullSafeGet(..) method
	    return null;
	} else if (hibernateUserType instanceof MoneyUserType) {
	    return new Money((BigDecimal) values.get(0), (Currency) values.get(1));
	} else if (hibernateUserType instanceof MoneyWithTaxAmountUserType) {
	    return new Money((BigDecimal) values.get(0), (BigDecimal) values.get(1), (Currency) values.get(2));
	} else if (hibernateUserType instanceof SimpleMoneyType) {
	    return new Money((BigDecimal) values.get(0), Currency.getInstance(Locale.getDefault()));
	} else if (hibernateUserType instanceof SimplyMoneyWithTaxAmountType) {
	    return new Money((BigDecimal) values.get(0), (BigDecimal) values.get(1), Currency.getInstance(Locale.getDefault()));
	} else if (hibernateUserType instanceof SimplyMoneyWithTaxAndExTaxAmountType) {
	    return new Money(((BigDecimal) values.get(0)).add((BigDecimal) values.get(1)), (BigDecimal) values.get(1), Currency.getInstance(Locale.getDefault()));
	} else {
	    throw new RuntimeException("Unrecognised composite user type: " + hibernateUserType);
	}
    }

    private Object getAdoptedValueForPrimProp(final Class propertyType, final Object rawValue) {
	if (rawValue == null) {
	    return rawValue;
	} else if (propertyType.equals(Money.class)) {
	    return new Money(rawValue.toString());
	} else if (propertyType.equals(BigDecimal.class)) {
	    return new BigDecimal(rawValue.toString());
	} else if (propertyType.equals(BigInteger.class)) {
	    return new BigInteger(rawValue.toString());
	} else if (propertyType == boolean.class || propertyType == Boolean.class) {
	    return "Y".equalsIgnoreCase(rawValue.toString()) ? true : false;
	} else {
	    return rawValue;
	}
    }

    private boolean isHibernateType(final Class type) {
	return type != null && org.hibernate.type.Type.class.isAssignableFrom(type);
    }

    private boolean isHibernateUserType(final Class type) {
	return type != null && org.hibernate.usertype.UserType.class.isAssignableFrom(type);
    }

    private EntityContainer<E> transformTuple(final Class resultType, final Map<String, ColumnInfo> primProps, final Map<String, IEntityMapper> entityProps, final Map<String, NativeData> data, final boolean shouldBeFetched) {
	EntityContainer<E> entCont;
	if (resultType.equals(EntityAggregates.class)) {
	    entCont = new EntityContainer<E>(resultType, null, shouldBeFetched);
	    for (final Map.Entry<String, ColumnInfo> primProp : primProps.entrySet()) {
		final NativeData dataEntry = data.get(primProp.getValue().getColumnAlias());
		final Object primRawValue = dataEntry.data;
		if (primProp.getValue() instanceof ColumnInfoForEntityProp && primRawValue != null) {
		    entCont.entities.put(primProp.getKey(), new EntityContainer(dataEntry.typesInfo.getHibEntityType(), primRawValue, shouldBeFetched));
		} else {
		    entCont.primitives.put(primProp.getKey(), data.get(primProp.getValue().getColumnAlias()).data);
		}
	    }
	} else if (IQueryModelProvider.class.isAssignableFrom(resultType)) {
	    entCont = new EntityContainer<E>(resultType, null, shouldBeFetched);
	    for (final Map.Entry<String, ColumnInfo> primProp : primProps.entrySet()) {
		final Class propertyType = PropertyTypeDeterminator.determinePropertyType(resultType, primProp.getKey());
		final NativeData dataEntry = data.get(primProp.getValue().getColumnAlias());
		final Object primRawValue = dataEntry.data;
		if (primProp.getValue() instanceof ColumnInfoForEntityProp && primRawValue != null) {
		    entCont.entities.put(primProp.getKey(), new EntityContainer(dataEntry.typesInfo.getHibEntityType(), primRawValue, shouldBeFetched));
		} else {
		    logger.info("retrieving value for prop [" + primProp.getKey() + "@" + propertyType + "] : ["
			    + getAdoptedValueForPrimProp(propertyType, data.get(primProp.getValue().getColumnAlias()).data) + "]");
		    entCont.primitives.put(primProp.getKey(), getAdoptedValueForPrimProp(propertyType, data.get(primProp.getValue().getColumnAlias()).data));
		}
	    }
	} else {
	    if (data.get(primProps.get("id").getColumnAlias()).data == null) {
		return null;
	    }
	    entCont = new EntityContainer<E>(resultType, data.get(primProps.get("id").getColumnAlias()).data, shouldBeFetched);
	    final Map<String, Map<String, NativeData>> customUserTypeProps = new HashMap<String, Map<String, NativeData>>();
	    for (final Map.Entry<String, ColumnInfo> primProp : primProps.entrySet()) {
		if (primProp.getKey().contains(".") && !primProp.getKey().endsWith(".class")) {
		    final String[] parts = primProp.getKey().split("\\.");
		    if (parts.length != 2) {
			throw new RuntimeException("Should contain only two parts! But was " + parts.length);
		    }
		    if (!customUserTypeProps.containsKey(parts[0])) {
			customUserTypeProps.put(parts[0], new HashMap<String, NativeData>());
		    }
		    customUserTypeProps.get(parts[0]).put(parts[1], data.get(primProp.getValue().getColumnAlias()));
		} else {
		    if (!primProp.getKey().endsWith(".class")) {
			final NativeData dataEntry = data.get(primProp.getValue().getColumnAlias());
			final Object primRawValue = dataEntry.data;
			final Class dataType = dataEntry.typesInfo.getHibType(); // may be either Type or UserType
			final Class propertyType = PropertyTypeDeterminator.determinePropertyType(resultType, primProp.getKey());
			if (AbstractEntity.class.isAssignableFrom(propertyType) && primRawValue != null && isHibernateType(dataType)) {
			    if (!primProps.containsKey(primProp.getKey() + ".class")) {
				entCont.entities.put(primProp.getKey(), new EntityContainer(dataEntry.typesInfo.getHibEntityType(), primRawValue, shouldBeFetched));
			    } else {
				final NativeData classData = data.get(primProps.get(primProp.getKey() + ".class").getColumnAlias());
				entCont.entities.put(primProp.getKey(), new EntityContainer(classData.typesInfo.getHibPolymorhicTypes().get(classData.data), primRawValue, shouldBeFetched));
			    }
			} else {
			    if (isHibernateType(dataType)) {
				entCont.primitives.put(primProp.getKey(), primRawValue);
			    } else if (isHibernateUserType(dataType)) {
				UserType userType;
				try {
				    userType = (UserType) dataType.newInstance();
				} catch (final Exception e) {
				    throw new RuntimeException(e);
				}
				entCont.primitives.put(primProp.getKey(), convertFromHibernateUserType(userType, primRawValue));
			    } else {
				entCont.primitives.put(primProp.getKey(), primRawValue);
			    }
			}
		    }
		}
	    }
	    for (final Map.Entry<String, Map<String, NativeData>> entry : customUserTypeProps.entrySet()) {
		CompositeUserType customUserType;
		try {
		    customUserType = (CompositeUserType) entry.getValue().entrySet().iterator().next().getValue().typesInfo.getHibEntityType().newInstance();
		} catch (final Exception e) {
		    throw new RuntimeException(e);
		}
		final List<Object> results = new ArrayList<Object>();
		final String[] propertyNames = customUserType.getPropertyNames();
		for (int i = 0; i < propertyNames.length; i++) {
		    results.add(entry.getValue().get(propertyNames[i]).data);
		}
		entCont.primitives.put(entry.getKey(), convertFromHibernateCompositeUserType(customUserType, results));
	    }
	}

	if (entityProps != null) {
	    for (final Map.Entry<String, IEntityMapper> propEntityEntry : entityProps.entrySet()) {
		final IEntityMapper propEntityMapper = propEntityEntry.getValue();
		entCont.entities.put(propEntityEntry.getKey(), transformTuple(propEntityMapper.getPropType(), propEntityMapper.getPropertiesColumns(), propEntityMapper.getSubMappers(), data, false));
	    }
	}
	return entCont;
    }

    private List<E> instantiateFromContainers(final List<EntityContainer<E>> containers, final boolean userViewOnly) {
	logger.info("Instantiating from containers -- entity type is " + (containers.size() > 0 ? containers.get(0).resultType.getName() : "?"));
	final DateTime st = new DateTime();
	final List<E> result = new ArrayList<E>();
	for (final EntityContainer<E> entityContainer : containers) {
	    result.add(entityContainer.instantiate(entityFactory, userViewOnly));
	}
	final Period pd = new Period(st, new DateTime());
	logger.info("Done. Instantiating from containers -- entity type is " + (containers.size() > 0 ? containers.get(0).resultType.getName() : "?") + "\n Duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
	return result;
    }

    /**
     * Fetches the results of the specified page based on the request of the given instance of {@link IQueryOrderedModel}.
     *
     * @param queryModel
     * @param pageNumber
     * @param pageCapacity
     * @return
     */
    @SessionRequired
    public List<E> list(final Session session, final MappingExtractor mappingExtractor, final EntityFactory entityFactory, final IQueryOrderedModel<E> queryModel, final Integer pageNumber, final Integer pageCapacity, final fetch<E> fetchModel) {
	this.session = session;
	this.mappingExtractor = mappingExtractor;
	this.entityFactory = entityFactory;
	return instantiateFromContainers(listContainers(queryModel, pageNumber, pageCapacity, fetchModel), queryModel.isLightweight());
    }


    /**
     * Same as {@link #list(Session, MappingExtractor, EntityFactory, IQueryOrderedModel, Integer, Integer, fetch, IFilter)}, but with some default parameter values (list all and no fetch).
     *
     * @param session
     * @param mappingExtractor
     * @param entityFactory
     * @param queryModel
     * @return
     */
    public List<E> list(final Session session, final MappingExtractor mappingExtractor, final EntityFactory entityFactory, final IQueryOrderedModel<E> queryModel) {
	return list(session, mappingExtractor, entityFactory, queryModel, null, null, null);
    }


    @SessionRequired
    private List<EntityContainer<E>> listContainers(final IQueryOrderedModel<E> queryModel, final Integer pageNumber, final Integer pageCapacity, final fetch<E> fetchModel) {

	final ReturnedModelResult modelResult = queryModel.getFinalModelResult(mappingExtractor);
	final List<EntityContainer<E>> result = listAsIs(modelResult, pageNumber, pageCapacity);
	return enhance(result, enhanceFetchModelWithKeyProperties(fetchModel, modelResult.getResultType()), modelResult.getResultType());
    }

    private fetch<E> enhanceFetchModelWithKeyProperties(final fetch<E> fetchModel, final Class<E> entitiesType) {
	final fetch<E> enhancedFetchModel = fetchModel != null ? fetchModel : new fetch(entitiesType);
	final List<String> keyMemberNames = Finder.getFieldNames(Finder.getKeyMembers(entitiesType));
	for (final String keyProperty : keyMemberNames) {
	    final Class propType = PropertyTypeDeterminator.determinePropertyType(entitiesType, keyProperty);
	    if (AbstractEntity.class.isAssignableFrom(propType) && !enhancedFetchModel.getFetchModels().containsKey(keyProperty)) {
		enhancedFetchModel.with(keyProperty, new fetch(propType));
	    }
	}
	return enhancedFetchModel;
    }

    /**
     * Enhances entities according to provided fetch model.
     *
     * @param entities - entities that will enhanced
     * @param fetchModel
     * @param entitiesType
     * @return
     */
    private List<EntityContainer<E>> enhance(final List<EntityContainer<E>> entities, final fetch<E> fetchModel, final Class<E> entitiesType) {
	if (fetchModel != null) {
	    final Map<String, fetch> propertiesFetchModels = fetchModel.getFetchModels();

	    for (final Map.Entry<String, fetch> entry : propertiesFetchModels.entrySet()) {
		final String propName = entry.getKey();
		final fetch propFetchModel = entry.getValue();

		if (mappingExtractor.isNotPersisted(entitiesType) || //
			mappingExtractor.isManyToOne(entitiesType, propName) || //
			mappingExtractor.isAny(entitiesType, propName) || //
			mappingExtractor.isOneToOneDetails(entitiesType, propName) //
		) {
		    logger.debug("enhancing property [" + propName + "]");
		    enhanceProperty(entities, propName, propFetchModel);
		} else if (mappingExtractor.isSimpleValue(entitiesType, propName)) {
		    logger.debug("enhancing property [" + propName + "]: no enhancing is required because it is SimpleValue.");
		    // e.g. properties that getting here usually has hibernate custom user type and are not classic "entities", and doesn't require enhancing
		} else if (mappingExtractor.isCollection(entitiesType, propName)) {
		    final String parentPropName = mappingExtractor.getParentPropertyName(entitiesType, propName);
		    final Class propertyType = PropertyTypeDeterminator.determineClass(entitiesType, propName, true, false);
		    String indexPropName = null;
		    if (mappingExtractor.isList(entitiesType, propName)) {
			indexPropName = mappingExtractor.getIndexPropertyName(entitiesType, propName);
		    }
		    logger.debug("enhancing collectional property [" + propName + "]");
		    enhanceCollectional(entities, propName, propertyType, parentPropName, indexPropName, propFetchModel);
		} else {
		    throw new IllegalArgumentException("Unsupported mapping type: === parent entity type is " + entitiesType + "; property name is " + propName);
		}
	    }
	}

	return entities;
    }

    /**
     * Iterates through provided entities and collects information on the requested property - property value and list of all entities with such property
     * value.
     *
     * @param entities
     * @param propertyName
     * @return
     */
    private Map<Long, List<EntityContainer<E>>> getEntityPropertyIds(final List<EntityContainer<E>> entities, final String propertyName) {
	final Map<Long, List<EntityContainer<E>>> propValuesMap = new HashMap<Long, List<EntityContainer<E>>>();
	logger.info("getting ids for property: " + propertyName);
	for (final EntityContainer<E> entity : entities) {
	    final EntityContainer propEntity = entity.entities.get(propertyName);
	    if (propEntity != null && propEntity.id != null) {
		if (!propValuesMap.containsKey(propEntity.id)) {
		    propValuesMap.put(propEntity.id, new ArrayList<EntityContainer<E>>());
		}
		propValuesMap.get(propEntity.id).add(entity);
	    }
	}
	return propValuesMap;
    }

    /**
     * Retrieves from the provided entity aggregates instances list of values for the specified property (of the specified type -- to cover polymorphic properties).
     *
     * @param entities
     * @param propName
     * @param propType
     * @return
     */
    private List<EntityContainer<AbstractEntity>> getRetrievedPropertyInstances(final List<EntityContainer<E>> entities, final String propName) {
	final List<EntityContainer<AbstractEntity>> propValues = new ArrayList<EntityContainer<AbstractEntity>>();
	for (final EntityContainer<?> entity : entities) {
	    final EntityContainer prop = entity.entities.get(propName);
	    if (prop != null && !prop.notYetInitialised()) {
		propValues.add(prop);
		prop.shouldBeFetched = true;
	    }
	}
	return propValues;
    }

    /**
     * Performs nested initialisation of the properties of the provided entities according to provided fetch model(s).
     *
     * @param entities
     * @param fetchModels
     * @return
     */
    private List<EntityContainer<E>> enhanceProperty(final List<EntityContainer<E>> entities, final String propertyName, final fetch fetchModel) {
	// Obtaining map between property id and list of entities where this property occurs
	final Map<Long, List<EntityContainer<E>>> propertyValuesIds = getEntityPropertyIds(entities, propertyName);
	logger.info("got ids count: " + propertyValuesIds.size());

	if (propertyValuesIds.size() > 0) {
	    // Constructing model for retrieving property instances based on the provided fetch model and list of instances ids
	    final List<EntityContainer<AbstractEntity>> retrievedPropertyInstances = getRetrievedPropertyInstances(entities, propertyName);
	    logger.info("got retrievedPropertyInstances count: " + retrievedPropertyInstances.size());
	    final List<EntityContainer> enhancedPropInstances;
	    if (retrievedPropertyInstances.size() == 0) {
		enhancedPropInstances = getDataInBatches(new ArrayList<Long>(propertyValuesIds.keySet()), fetchModel);
	    } else {
		enhancedPropInstances = new Fetcher(session, mappingExtractor, entityFactory).enhance(retrievedPropertyInstances, fetchModel, fetchModel.getEntityType());
	    }

	    // Replacing in entities the proxies of properties with properly enhanced property instances.
	    for (final EntityContainer enhancedPropInstance : enhancedPropInstances) {
		final List<EntityContainer<E>> thisPropertyEntities = propertyValuesIds.get(enhancedPropInstance.getId());
		for (final EntityContainer<E> thisPropertyEntity : thisPropertyEntities) {
		    thisPropertyEntity.entities.put(propertyName, enhancedPropInstance);
		}
	    }
	}

	return entities;
    }

    private List<EntityContainer> getDataInBatches(final List<Long> ids, final fetch<E> fetchModel) {
	final List<EntityContainer> result = new ArrayList<Fetcher.EntityContainer>();
	final Long[] allParentIds = new ArrayList<Long>(ids).toArray(new Long[] {});

	final Integer batchSize = 990;
	Integer from = 0;
	Integer to = batchSize;
	boolean endReached = false;
	while (!endReached) {
	    if (to >= allParentIds.length) {
		endReached = true;
		to = allParentIds.length;
	    }
	    final Long[] batch = Arrays.copyOfRange(allParentIds, from, to);
	    @SuppressWarnings("unchecked")
	    final IQueryOrderedModel<AbstractEntity> currTypePropertyModel = select(fetchModel.getEntityType()).where().prop(ID_PROPERTY_NAME).in().val(batch).model().getModelWithAbstractEntities();
	    @SuppressWarnings("unchecked")
	    final List<EntityContainer> properties = new Fetcher(session, mappingExtractor, entityFactory).listContainers(currTypePropertyModel, null, null, fetchModel);
	    result.addAll(properties);
	    from = to;
	    to = to + batchSize;
	}

	return result;
    }

    /**
     * Performs nested initialisation of the collectional properties of the provided entities according to provided fetch model. It should be noted, that due to implementation
     * specifics the instances returned are not longer associated with current hibernate session. In case of necessity they should re-associated with session via merge method.
     *
     * @param entitiesToBeEnhanced
     * @param propertyName
     * @param parentPropName
     * @param indexPropName
     * @param propertyFetchModel
     * @return
     */
    private List<EntityContainer<E>> enhanceCollectional(final List<EntityContainer<E>> entitiesToBeEnhanced, final String propertyName, final Class propType, final String parentPropName, final String indexPropName, final fetch<E> fetchModel) {
	// collect parental ids
	final Map<Long, EntityContainer<E>> parentIds = new HashMap<Long, EntityContainer<E>>();
	for (final EntityContainer<E> parentEntity : entitiesToBeEnhanced) {
	    parentIds.put(parentEntity.getId(), parentEntity);
	}
	final List<EntityContainer> properties = getCollectionalDataInBatches(parentIds.keySet(), parentPropName, indexPropName, fetchModel);

	// group retrieved collections by parents
	@SuppressWarnings("unchecked")
	final Map<Long, List<EntityContainer>> results = new HashMap<Long, List<EntityContainer>>();
	for (final EntityContainer<?> collectionalItem : properties) {
	    final Long currentParentId = collectionalItem.entities.get(parentPropName).id;
	    if (!results.containsKey(currentParentId)) {
		results.put(currentParentId, new ArrayList<EntityContainer>());
	    }
	    // assign collectional item parent property reference to its already fetched parent
	    collectionalItem.entities.put(parentPropName, parentIds.get(currentParentId));
	    results.get(currentParentId).add(collectionalItem);
	}

	// set the retrieved data for those entities where collectional property is not empty
	for (final Map.Entry<Long, List<EntityContainer>> resultEntry : results.entrySet()) {
	    // assigns initialised collection to parent collectional property (lazy-collection is already evicted)
	    final EntityContainer<E> entity = parentIds.get(resultEntry.getKey());
	    if (List.class.isAssignableFrom(propType)) {
		entity.collections.put(propertyName, resultEntry.getValue());
	    } else if (Set.class.isAssignableFrom(propType)) {
		// TODO need to implement proper collection instantiation (prop type)
		entity.collections.put(propertyName, new HashSet<EntityContainer>(resultEntry.getValue()));
	    } else {
		throw new UnsupportedOperationException("Fetching via models for collections of type ["
			+ /* parentIds.get(resultEntry.getKey()).get(propertyName).getClass() */" xxx " + "] is not yet supported.");
	    }
	}

	return entitiesToBeEnhanced;
    }

    private List<EntityContainer> getCollectionalDataInBatches(final Set<Long> parentIds, final String parentPropName, final String indexPropName, final fetch<E> fetchModel) {
	final List<EntityContainer> result = new ArrayList<Fetcher.EntityContainer>();
	final String idProp = parentPropName  + "." + ID_PROPERTY_NAME;
	final Long[] allParentIds = new ArrayList<Long>(parentIds).toArray(new Long[]{});

	final Integer batchSize = 990;
	Integer from = 0;
	Integer to = batchSize;
	boolean endReached = false;
	while (!endReached) {
	    if (to >= allParentIds.length) {
		endReached = true;
		to = allParentIds.length;
	    }
	    final Long[] batch = Arrays.copyOfRange(allParentIds, from, to);
		@SuppressWarnings("unchecked")
		final ICompleted base = select(fetchModel.getEntityType()).where().prop(idProp).in().val(batch);
		final IQueryOrderedModel<AbstractEntity> currTypePropertyModel = (indexPropName != null ? base.orderBy(parentPropName, indexPropName) : base.orderBy(parentPropName)).model().getModelWithAbstractEntities();
		@SuppressWarnings("unchecked")
		// final List<EntityContainer> properties = new Fetcher().listContainersWithoutKeyEnhanced(currTypePropertyModel, null, null);
		final List<EntityContainer> properties = new Fetcher(session, mappingExtractor, entityFactory).listContainers(currTypePropertyModel, null, null, fetchModel);
		result.addAll(properties);
		// TODO need to optimise -- WagonClass in WagonClassCompatibility is re-retrieved, while already available
		from = to;
		to =  to + batchSize;
	}

	return result;
    }

    static class EntityContainer<R extends AbstractEntity> {
	Class<R> resultType;
	R entity;
	Long id;
	boolean shouldBeFetched;
	Map<String, Object> primitives = new HashMap<String, Object>();
	Map<String, EntityContainer> entities = new HashMap<String, EntityContainer>();
	Map<String, Collection<EntityContainer>> collections = new HashMap<String, Collection<EntityContainer>>();
	private Logger logger = Logger.getLogger(this.getClass());

	public EntityContainer(final Class resultType, final Object id, final boolean shouldBeFetched) {
	    this.resultType = resultType;
	    if (id != null) {
		this.id = ((Number) id).longValue();
	    }
	    this.shouldBeFetched = shouldBeFetched;
	}

	public boolean notYetInitialised() {
	    return primitives.size() + entities.size() + collections.size() == 0;
	}

	public boolean isInstantiated() {
	    return entity != null;
	}

	public Long getId() {
	    return id;
	}

	public R instantiate(final EntityFactory entFactory, final boolean userViewOnly) {
	    logger.info("instantiating: " + resultType.getName() + " for id = " + id + " lightWeight = " + userViewOnly);
	    entity = userViewOnly ? entFactory.newPlainEntity(resultType, id) : entFactory.newEntity(resultType, id);
	    entity.setInitialising(true);
	    for (final Map.Entry<String, Object> primPropEntry : primitives.entrySet()) {
		try {
		    setPropertyValue(entity, primPropEntry.getKey(), primPropEntry.getValue(), userViewOnly);
		    //entity.set(primPropEntry.getKey(), primPropEntry.getValue());
		} catch (final Exception e) {
		    throw new IllegalStateException("Can't set value [" + primPropEntry.getValue() + "] of type ["
			    + (primPropEntry.getValue() != null ? primPropEntry.getValue().getClass().getName() : " unknown") + "] for property [" + primPropEntry.getKey()
			    + "] due to:" + e);
		}
	    }

	    for (final Map.Entry<String, EntityContainer> entityEntry : entities.entrySet()) {
		if (entityEntry.getValue() == null || entityEntry.getValue().notYetInitialised() || !entityEntry.getValue().shouldBeFetched) {
		    setPropertyValue(entity, entityEntry.getKey(), null,  userViewOnly);
		} else if (entityEntry.getValue().isInstantiated()) {
		    setPropertyValue(entity, entityEntry.getKey(), entityEntry.getValue().entity, userViewOnly);
		} else {
		    setPropertyValue(entity, entityEntry.getKey(), entityEntry.getValue().instantiate(entFactory, userViewOnly), userViewOnly);
		}
	    }

	    for (final Map.Entry<String, Collection<EntityContainer>> entityEntry : collections.entrySet()) {
		Collection collectionalProp = null;
		try {
		    collectionalProp = entityEntry.getValue().getClass().newInstance();
		} catch (final Exception e) {
		    throw new RuntimeException("COULD NOT EXECUTE [collectionalProp = entityEntry.getValue().getClass().newInstance();] due to: " + e);
		}
		for (final EntityContainer container : entityEntry.getValue()) {
		    if (!container.notYetInitialised()) {
			collectionalProp.add(container.instantiate(entFactory, userViewOnly));
		    }
		}
		setPropertyValue(entity, entityEntry.getKey(), collectionalProp, userViewOnly);
	    }

	    if (!userViewOnly) {
		EntityUtils.handleMetaProperties(entity);
	    }

	    entity.setInitialising(false);

	    return entity;
	}

	private void setPropertyValue(final AbstractEntity entity, final String propName, final Object propValue, final boolean userViewOnly) {
	    if (!userViewOnly || EntityAggregates.class.isAssignableFrom(resultType)) {
		entity.set(propName, propValue);
	    } else {
		try {
		    final Field field = Finder.findFieldByName(resultType, propName);
		    field.setAccessible(true);
		    if (propValue == null && Money.class.isAssignableFrom(field.getType())) {
			field.set(entity, new Money("0"));
		    } else {
			field.set(entity, propValue);
		    }
		    field.setAccessible(false);
		} catch (final Exception e) {
		    throw new RuntimeException("Can't set value for property " + propName + " due to:" + e.getMessage());
		}
	    }
	}
    }

    static class NativeData {
	Object data;
	TypesInfo typesInfo;

	NativeData(final Object data, final TypesInfo typesInfo) {
	    this.data = data;
	    this.typesInfo = typesInfo;
	}

	@Override
	public String toString() {
	    return data + " : " + typesInfo.getHibType() + " : " + typesInfo.getHibEntityType();
	}
    }
}
