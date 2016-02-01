package ua.com.fielden.platform.swing.review.development;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.criteria.enhanced.CriteriaProperty;
import ua.com.fielden.platform.criteria.enhanced.SecondParam;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.dao.ILifecycleDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation.IAddToCriteriaTickRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer.ByteArray;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.swing.review.DynamicFetchBuilder;
import ua.com.fielden.platform.swing.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.swing.review.DynamicParamBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;

import com.google.inject.Inject;

@KeyType(String.class)
public abstract class EntityQueryCriteria<C extends ICentreDomainTreeManagerAndEnhancer, T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends AbstractEntity<String> {

    private static final long serialVersionUID = 9154466083364529734L;

    @SuppressWarnings("rawtypes")
    private final Map<String, IValueMatcher> valueMatchers = new HashMap<String, IValueMatcher>();
    private final IValueMatcherFactory valueMatcherFactory;

    private final DAO dao;
    private final IGeneratedEntityController<T> generatedEntityController;

    private final ISerialiser serialiser;

    private final C cdtme;
    private final ICompanionObjectFinder controllerProvider;
    private Optional<IFetchProvider<T>> additionalFetchProvider = Optional.empty();
    private Optional<IQueryEnhancer<T>> additionalQueryEnhancer = Optional.empty();
    private Optional<CentreContext<T, ?>> centreContextForQueryEnhancer = Optional.empty();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Inject
    public EntityQueryCriteria(final IValueMatcherFactory valueMatcherFactory, final IGeneratedEntityController generatedEntityController, final ISerialiser serialiser, final ICompanionObjectFinder controllerProvider) {
        this.valueMatcherFactory = valueMatcherFactory;
        this.generatedEntityController = generatedEntityController;
        this.serialiser = serialiser;
        this.controllerProvider = controllerProvider;

        //This values should be initialized through reflection.
        this.dao = null;
        this.cdtme = null;
    }

    public DAO companionObject() {
        return dao;
    }

    /**
     * Returns the centre domain tree manager.
     *
     * @return
     */
    public C getCentreDomainTreeMangerAndEnhancer() {
        return cdtme;
    }

    /**
     * Returns the copy of centre domain tree manager
     *
     * @return
     */
    //TODO remove this later after details will be implemented using
    public C getCentreDomainTreeManagerAndEnhnacerCopy() {
        return EntityUtils.deepCopy(cdtme, serialiser);
    }

    /**
     * Returns the root for which this criteria was generated.
     *
     * @return
     */
    public Class<T> getEntityClass() {
        return dao.getEntityType();
    }

    /**
     * Returns the enhanced type for entity class. The entity class is retrieved with {@link #getEntityClass()} method.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public Class<T> getManagedType() {
        return (Class<T>) getCentreDomainTreeMangerAndEnhancer().getEnhancer().getManagedType(getEntityClass());
    }

    /**
     * Must load default values for the properties of the binding entity.
     */
    public void defaultValues() {
        final IAddToCriteriaTickRepresentation ftr = getCentreDomainTreeMangerAndEnhancer().getRepresentation().getFirstTick();
        for (final Field propertyField : CriteriaReflector.getCriteriaProperties(getType())) {
            final SecondParam secondParam = AnnotationReflector.getAnnotation(propertyField, SecondParam.class);
            final CriteriaProperty critProperty = AnnotationReflector.getAnnotation(propertyField, CriteriaProperty.class);
            final Class<T> root = getEntityClass();
            set(propertyField.getName(), secondParam == null ? ftr.getValueByDefault(root, critProperty.propertyName()) : ftr.getValue2ByDefault(root, critProperty.propertyName()));
        }
    }

    @SuppressWarnings("unchecked")
    public IValueMatcher<?> getValueMatcher(final String propertyName) {
        if (valueMatchers.get(propertyName) == null) {
            valueMatchers.put(propertyName, valueMatcherFactory.getValueMatcher(getType(), propertyName));
        }
        return valueMatchers.get(propertyName);
    }

    /**
     * Enhances this criteria entity with custom fetch provider, that will extend the fetching strategy of running queries on top of chosen result-set properties.
     *
     * @param additionalFetchProvider
     */
    public void setAdditionalFetchProvider(final IFetchProvider<T> additionalFetchProvider) {
        this.additionalFetchProvider = Optional.of(additionalFetchProvider);
    }

    /**
     * Enhances this criteria entity with custom query enhancer and its optional centre context, that will extend the query on top of chosen criteria conditions.
     *
     * @param additionalQueryEnhancer
     * @param centreContextForQueryEnhancer
     */
    public void setAdditionalQueryEnhancerAndContext(final IQueryEnhancer<T> additionalQueryEnhancer, final Optional<CentreContext<T, ?>> centreContextForQueryEnhancer) {
        this.additionalQueryEnhancer = Optional.of(additionalQueryEnhancer);
        this.centreContextForQueryEnhancer = centreContextForQueryEnhancer;
    }

    /**
     * Creates the query model based on 'queryProperties' of the 'managedType' and on custom query enhancer (if any).
     *
     * @param managedType
     * @param queryProperties
     * @param additionalQueryEnhancer
     * @param centreContextForQueryEnhancer
     * @return
     */
    private static <T extends AbstractEntity<?>> EntityResultQueryModel<T> createQuery(final Class<T> managedType, final List<QueryProperty> queryProperties, final Optional<IQueryEnhancer<T>> additionalQueryEnhancer, final Optional<CentreContext<T, ?>> centreContextForQueryEnhancer) {
        if (additionalQueryEnhancer.isPresent()) {
            return DynamicQueryBuilder.createQuery(managedType, queryProperties, Optional.of(new Pair<>(additionalQueryEnhancer.get(), centreContextForQueryEnhancer))).model();
        } else {
            return DynamicQueryBuilder.createQuery(managedType, queryProperties).model();
        }
    }

    /**
     * Creates the fetch model based on 'properties' of the 'managedType' and on custom fetch provider (if any).
     *
     * @param managedType
     * @param properties
     * @param additionalFetchProvider
     * @return
     */
    private static <T extends AbstractEntity<?>, V extends AbstractEntity<?>> fetch<V> createFetchModelFrom(final Class<V> managedType, final Set<String> properties, final Optional<IFetchProvider<T>> additionalFetchProvider) {
        final IFetchProvider<V> rootProvider = properties.contains("") ? EntityUtils.fetchWithKeyAndDesc(managedType) : EntityUtils.fetch(managedType);
        final IFetchProvider<V> rootProviderWithResultSetProperties = rootProvider.with(properties);
        if (additionalFetchProvider.isPresent()) {
            return rootProviderWithResultSetProperties.with(additionalFetchProvider.get().copy(managedType)).fetchModel();
        } else {
            return rootProviderWithResultSetProperties.fetchModel();
        }
    }

    /**
     * This is temporary solution needed for pagination support on web ui
     */
    public IPage<T> getPage(final int pageNumber, final int pageCount, final int pageCapacity) {
        final Class<?> root = getEntityClass();
        final IAddToResultTickManager resultTickManager = getCentreDomainTreeMangerAndEnhancer().getSecondTick();
        final IAddToCriteriaTickManager criteriaTickManager = getCentreDomainTreeMangerAndEnhancer().getFirstTick();
        final IDomainTreeEnhancer enhancer = getCentreDomainTreeMangerAndEnhancer().getEnhancer();
        final Pair<Set<String>, Set<String>> separatedFetch = EntityQueryCriteriaUtils.separateFetchAndTotalProperties(root, resultTickManager, enhancer);
        final Map<String, Pair<Object, Object>> paramMap = EntityQueryCriteriaUtils.createParamValuesMap(getEntityClass(), getManagedType(), criteriaTickManager);
        final EntityResultQueryModel<T> notOrderedQuery = createQuery(getManagedType(), createQueryProperties(), additionalQueryEnhancer, centreContextForQueryEnhancer);
        final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = fromWithLight(notOrderedQuery)
        .with(DynamicOrderingBuilder.createOrderingModel(getManagedType(), resultTickManager.orderedProperties(root)))//
        .with(createFetchModelFrom(getManagedType(), separatedFetch.getKey(), additionalFetchProvider))//
        .with(enhanceQueryParams(DynamicParamBuilder.buildParametersMap(getManagedType(), paramMap))).model();
        if (getManagedType().equals(getEntityClass())) {
            return dao.getPage(resultQuery, pageNumber, pageCount, pageCapacity);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.getPage(resultQuery, pageNumber, pageCount, pageCapacity, getByteArrayForManagedType());
        }
    }

    private Map<String, Object> enhanceQueryParams(final Map<String, Object> buildParametersMap) {
        if (additionalQueryEnhancer.isPresent()) {
            return additionalQueryEnhancer.get().enhanceQueryParams(buildParametersMap, centreContextForQueryEnhancer);
        }
        return buildParametersMap;
    }

    /**
     * Run the configured entity query.
     *
     * @param pageSize
     * @return
     */
    public final IPage<T> run(final int pageSize) {
        final Class<?> root = getEntityClass();
        final IAddToResultTickManager resultTickManager = getCentreDomainTreeMangerAndEnhancer().getSecondTick();
        final IAddToCriteriaTickManager criteriaTickManager = getCentreDomainTreeMangerAndEnhancer().getFirstTick();
        final IDomainTreeEnhancer enhancer = getCentreDomainTreeMangerAndEnhancer().getEnhancer();
        final Pair<Set<String>, Set<String>> separatedFetch = EntityQueryCriteriaUtils.separateFetchAndTotalProperties(root, resultTickManager, enhancer);
        final Map<String, Pair<Object, Object>> paramMap = EntityQueryCriteriaUtils.createParamValuesMap(getEntityClass(), getManagedType(), criteriaTickManager);
        final EntityResultQueryModel<T> notOrderedQuery = createQuery(getManagedType(), createQueryProperties(), additionalQueryEnhancer, centreContextForQueryEnhancer);
        final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = fromWithLight(notOrderedQuery)
        .with(DynamicOrderingBuilder.createOrderingModel(getManagedType(), resultTickManager.orderedProperties(root)))//
        .with(createFetchModelFrom(getManagedType(), separatedFetch.getKey(), additionalFetchProvider))//
        .with(enhanceQueryParams(DynamicParamBuilder.buildParametersMap(getManagedType(), paramMap))).model();
        if (!separatedFetch.getValue().isEmpty()) {
            final QueryExecutionModel<T, EntityResultQueryModel<T>> totalQuery = fromWithLight(notOrderedQuery)
            .with(DynamicFetchBuilder.createTotalFetchModel(getManagedType(), separatedFetch.getValue()))//
            .with(enhanceQueryParams(DynamicParamBuilder.buildParametersMap(getManagedType(), paramMap))).model();
            return firstPage(resultQuery, totalQuery, pageSize);
        } else {
            return run(resultQuery, pageSize);
        }
    }

    /**
     * Runs the specified query and returns result.
     *
     * @param queryModel
     * @param pageSize
     * @return
     */
    public final IPage<T> run(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final int pageSize) {
        return firstPage(queryModel, pageSize);
    }

    /**
     * Runs the specified query model and returns the result list.
     *
     * @param queryModel
     * @return
     */
    public final List<T> run(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel) {
        return getAllEntities(queryModel);
    }

    /**
     * Runs the specified query model with managed type and its byte representation. Returns the result page of specified size.
     *
     * @param queryModel
     * @param managedType
     * @param managedTypeArray
     * @param pageSize
     * @return
     */
    public final IPage<T> run(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final Class<T> managedType, final byte[] managedTypeArray, final int pageSize) {
        generatedEntityController.setEntityType(managedType);
        final List<byte[]> byteArrayList = getByteArrayForManagedType();
        byteArrayList.add(managedTypeArray);
        return generatedEntityController.firstPage(queryModel, pageSize, byteArrayList);
    }

    /**
     * Runs the specified query model with managed type and its byte representation.
     *
     * @param queryModel
     * @param managedType
     * @param managedTypeArray
     * @return
     */
    public final List<T> run(final ICentreDomainTreeManagerAndEnhancer cdtmeWithWhichAnalysesQueryHaveBeenCreated, final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final Class<T> managedType, final byte[] managedTypeArray) {
        generatedEntityController.setEntityType(managedType);
        // TODO here cdtme from this instance is magically DIFFERENT from cdtme, on which query has been created. So byte arrays will be taken from original Cdtme. Should that Cdtmes be the same??
        // TODO here cdtme from this instance is magically DIFFERENT from cdtme, on which query has been created. So byte arrays will be taken from original Cdtme. Should that Cdtmes be the same??
        // TODO here cdtme from this instance is magically DIFFERENT from cdtme, on which query has been created. So byte arrays will be taken from original Cdtme. Should that Cdtmes be the same??
        final List<byte[]> byteArrayList = cdtmeWithWhichAnalysesQueryHaveBeenCreated == null ? getByteArrayForManagedType()
                : toByteArray(/*getCentreDomainTreeMangerAndEnhancer()*/cdtmeWithWhichAnalysesQueryHaveBeenCreated.getEnhancer().getManagedTypeArrays(getEntityClass())); // getByteArrayForManagedType();
        byteArrayList.add(managedTypeArray);
        return generatedEntityController.getAllEntities(queryModel, byteArrayList);
    }

    /**
     * Returns the {@link LifecycleModel} instance that corresponds to the given query model, lifecycle property and the interval. If the associated controller isn't lifecycle then
     * it will return null.
     *
     * @param model
     * @param propertyName
     * @param from
     * @param to
     * @return
     */
    @SuppressWarnings("unchecked")
    public final LifecycleModel<T> getLifecycleInformation(final EntityResultQueryModel<? extends AbstractEntity<?>> model, final List<String> distributionProperties, final String propertyName, final DateTime from, final DateTime to) {
        if (isLifecycleController()) {
            return ((ILifecycleDao<T>) dao).getLifecycleInformation(model, getByteArrayForManagedType(), distributionProperties, propertyName, from, to);
        } else {
            return null;
        }
    }

    /**
     * Returns the value that indicates whether companion object associated with this criteria is lifecycle or not.
     *
     * @return
     */
    public boolean isLifecycleController() {
        return dao instanceof ILifecycleDao;
    }

    /**
     * Exports data in to the specified external file.
     *
     * @param fileName
     * @param propertyNames
     * @param propertyTitles
     */
    public void export(final String fileName, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        final Class<?> root = getEntityClass();
        final IAddToCriteriaTickManager criteriaTickManager = getCentreDomainTreeMangerAndEnhancer().getFirstTick();
        final IAddToResultTickManager tickManager = getCentreDomainTreeMangerAndEnhancer().getSecondTick();
        final IDomainTreeEnhancer enhancer = getCentreDomainTreeMangerAndEnhancer().getEnhancer();
        final Pair<Set<String>, Set<String>> separatedFetch = EntityQueryCriteriaUtils.separateFetchAndTotalProperties(root, tickManager, enhancer);
        final Map<String, Pair<Object, Object>> paramMap = EntityQueryCriteriaUtils.createParamValuesMap(getEntityClass(), getManagedType(), criteriaTickManager);
        final EntityResultQueryModel<T> notOrderedQuery = createQuery(getManagedType(), createQueryProperties(), additionalQueryEnhancer, centreContextForQueryEnhancer);
        final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = fromWithLight(notOrderedQuery)
        .with(DynamicOrderingBuilder.createOrderingModel(getManagedType(), tickManager.orderedProperties(root)))//
        .with(createFetchModelFrom(getManagedType(), separatedFetch.getKey(), additionalFetchProvider))//
        .with(enhanceQueryParams(DynamicParamBuilder.buildParametersMap(getManagedType(), paramMap))).model();
        export(fileName, resultQuery, propertyNames, propertyTitles);
    }

    private QueryExecutionModel.Builder<T, EntityResultQueryModel<T>> fromWithLight(final EntityResultQueryModel<T> notOrderedQuery) {
        return IAceHandlersAware.class.isAssignableFrom(getEntityClass()) ? from(notOrderedQuery) : from(notOrderedQuery).lightweight();
    }

    /**
     * Exports data, those were retrieved with query, in to the file specified with appropriate filename.
     *
     * @param fileName
     * @param query
     * @param propertyNames
     * @param propertyTitles
     * @throws IOException
     */
    public void export(final String fileName, final QueryExecutionModel<T, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        final byte[] content;
        query.lightweight();
        if (getManagedType().equals(getEntityClass())) {
            content = dao.export(query, propertyNames, propertyTitles);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            content = generatedEntityController.export(query, propertyNames, propertyTitles, getByteArrayForManagedType());
        }
        final FileOutputStream fo = new FileOutputStream(fileName);
        fo.write(content);
        fo.flush();
        fo.close();
    }

    /**
     * Returns the entity for specified id
     *
     * @param entity
     * @return
     */
    public T getEntityById(final Long id) {
        final Class<?> root = getEntityClass();
        final IAddToResultTickManager tickManager = getCentreDomainTreeMangerAndEnhancer().getSecondTick();
        final IDomainTreeEnhancer enhancer = getCentreDomainTreeMangerAndEnhancer().getEnhancer();
        final Pair<Set<String>, Set<String>> separatedFetch = EntityQueryCriteriaUtils.separateFetchAndTotalProperties(root, tickManager, enhancer);
        final fetch<T> fetchModel = createFetchModelFrom(getManagedType(), separatedFetch.getKey(), additionalFetchProvider);
        if (getManagedType().equals(getEntityClass())) {
            return dao.findById(id, fetchModel);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.findById(id, fetchModel, getByteArrayForManagedType());
        }
    }

    /**
     * Removes the passed entity. If the entity is not of the entity type or managed type of this criteria.
     *
     * @param entity
     */
    public void delete(final T entity) {
        if (entity.getType().equals(getEntityClass())) {
            dao.delete(entity);
        } else {
            throw new IllegalArgumentException("Unexpeted entity type " + entity.getType() + " preventing deletion.");
        }
    }

    /**
     * Returns refetched with fetchModel entity for specified one.
     *
     * @param entity
     * @param fetchModel
     * @return
     */
    public T refetchEntity(final T entity, final fetch<T> fetchModel) {
        if (entity.getType().equals(getEntityClass())) {
            return dao.findByEntityAndFetch(fetchModel, entity);
        } else if (DynamicEntityClassLoader.isEnhanced(entity.getType()) && DynamicEntityClassLoader.getOriginalType(entity.getType()).equals(getEntityClass())) {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.findById(entity.getId(), fetchModel, getByteArrayForManagedType());
        }
        throw new IllegalArgumentException("The entity type is incorrect. The entity type must be: " + getEntityClass() + " but was "
                + DynamicEntityClassLoader.getOriginalType(entity.getType()).equals(getEntityClass()));
    }

    /**
     * Converts existing properties model (which has separate properties for from/to, is/isNot and so on) into new properties model (which has single abstraction for one
     * criterion).
     *
     * @param properties
     * @return
     */
    public final List<QueryProperty> createQueryProperties() {
        final List<QueryProperty> queryProperties = new ArrayList<QueryProperty>();
        for (final String actualProperty : getCentreDomainTreeMangerAndEnhancer().getFirstTick().checkedProperties(getEntityClass())) {
            if (!AbstractDomainTree.isPlaceholder(actualProperty)) {
                queryProperties.add(createQueryProperty(actualProperty));
            }
        }
        return queryProperties;
    }

    /**
     * Returns the first result page for query model. The page size is specified with the second parameter.
     *
     * @param queryModel
     *            - query model for which the first result page must be returned.
     * @param pageSize
     *            - the page size.
     * @return
     */
    public final IPage<T> firstPage(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final int pageSize) {
        if (getManagedType().equals(getEntityClass())) {
            return dao.firstPage(queryModel, pageSize);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.firstPage(queryModel, pageSize, getByteArrayForManagedType());
        }
    }

    /**
     * Returns all entities those satisfies conditions of the specified {@link QueryExecutionModel}.
     *
     * @param queryModel
     *            - query model for which the first result page must be returned.
     * @return
     */
    public final List<T> getAllEntities(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel) {
        if (getManagedType().equals(getEntityClass())) {
            return dao.getAllEntities(queryModel);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.getAllEntities(queryModel, getByteArrayForManagedType());
        }
    }

    /**
     * Returns first entities those satisfies conditions of the specified {@link QueryExecutionModel}.
     *
     * @param queryModel
     *            - query model for which the first result page must be returned.
     * @return
     */
    public final List<T> getFirstEntities(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final int numberOfEntities) {
        if (getManagedType().equals(getEntityClass())) {
            return dao.getFirstEntities(queryModel, numberOfEntities);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.getFirstEntities(queryModel, numberOfEntities, getByteArrayForManagedType());
        }
    }

    /**
     * Returns the first result page for query model with summary. The page size is specified with the third parameter.
     *
     * @param queryModel
     *            - query model for which the first result page must be returned.
     * @param pageSize
     *            - the page size.
     * @return
     */
    public final IPage<T> firstPage(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final QueryExecutionModel<T, EntityResultQueryModel<T>> totalsModel, final int pageSize) {
        if (getManagedType().equals(getEntityClass())) {
            return dao.firstPage(queryModel, pageSize);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.firstPage(queryModel, totalsModel, pageSize, getByteArrayForManagedType());
        }
    }

    /**
     * Converts existing properties model (which has separate properties for from/to, is/isNot and so on) into new properties model (which has single abstraction for one
     * criterion).
     *
     * @param properties
     * @return
     */
    private QueryProperty createQueryProperty(final String actualProperty) {
        final IAddToCriteriaTickManager tickManager = getCentreDomainTreeMangerAndEnhancer().getFirstTick();
        final Class<T> root = getEntityClass();
        final QueryProperty queryProperty = EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(getManagedType(), actualProperty);

        queryProperty.setValue(tickManager.getValue(root, actualProperty));
        if (AbstractDomainTree.isDoubleCriterionOrBoolean(getManagedType(), actualProperty)) {
            queryProperty.setValue2(tickManager.getValue2(root, actualProperty));
        }
        if (AbstractDomainTree.isDoubleCriterion(getManagedType(), actualProperty)) {
            queryProperty.setExclusive(tickManager.getExclusive(root, actualProperty));
            queryProperty.setExclusive2(tickManager.getExclusive2(root, actualProperty));
        }
        final Class<?> propertyType = StringUtils.isEmpty(actualProperty) ? getManagedType() : PropertyTypeDeterminator.determinePropertyType(getManagedType(), actualProperty);
        if (EntityUtils.isDate(propertyType)) {
            queryProperty.setDatePrefix(tickManager.getDatePrefix(root, actualProperty));
            queryProperty.setDateMnemonic(tickManager.getDateMnemonic(root, actualProperty));
            queryProperty.setAndBefore(tickManager.getAndBefore(root, actualProperty));
        }
        queryProperty.setOrNull(tickManager.getOrNull(root, actualProperty));
        queryProperty.setNot(tickManager.getNot(root, actualProperty));
        return queryProperty;
    }

    /**
     * Returns the byte array for the managed type.
     *
     * @return
     */
    private List<byte[]> getByteArrayForManagedType() {
        return toByteArray(getCentreDomainTreeMangerAndEnhancer().getEnhancer().getManagedTypeArrays(getEntityClass()));
    }

    /**
     * Returns the list of byte arrays for the list of {@link ByteArray} instances.
     *
     * @param list
     * @return
     */
    private List<byte[]> toByteArray(final List<ByteArray> list) {
        final List<byte[]> byteArray = new ArrayList<byte[]>(list.size());
        for (final ByteArray array : list) {
            byteArray.add(array.getArray());
        }
        return byteArray;
    }

    public IGeneratedEntityController<T> getGeneratedEntityController() {
        return generatedEntityController;
    }

    public ISerialiser getSerialiser() {
        return serialiser;
    }

    /**
     * @return the default <code>controllerProvider</code>.
     */
    public ICompanionObjectFinder getControllerProvider() {
        return controllerProvider;
    }
}