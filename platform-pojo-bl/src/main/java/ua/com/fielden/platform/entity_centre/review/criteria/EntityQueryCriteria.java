package ua.com.fielden.platform.entity_centre.review.criteria;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isBooleanCriterion;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isDoubleCriterion;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isPlaceholder;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isShortCollection;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity_centre.review.DynamicParamBuilder.buildParametersMap;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty.queryPropertyParamName;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteriaUtils.createParamValuesMap;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isDotNotation;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.penultAndLast;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.transform;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import com.google.inject.Inject;

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
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicFetchBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.pagination.EmptyPage;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;

@KeyType(String.class)
public abstract class EntityQueryCriteria<C extends ICentreDomainTreeManagerAndEnhancer, T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends AbstractEntity<String> {

    private final DAO dao;
    private final IGeneratedEntityController<T> generatedEntityController;

    private final ISerialiser serialiser;

    private final C cdtme;
    private final ICompanionObjectFinder controllerProvider;
    private Optional<IFetchProvider<T>> additionalFetchProvider = Optional.empty();
    private Optional<IFetchProvider<T>> additionalFetchProviderForTooltipProperties = Optional.empty();
    private Optional<IQueryEnhancer<T>> additionalQueryEnhancer = Optional.empty();
    private Optional<CentreContext<T, ?>> centreContextForQueryEnhancer = Optional.empty();
    private Optional<User> createdByUserConstraint = Optional.empty();
    private final List<List<DynamicColumnForExport>> dynamicProperties = new ArrayList<>();
    private AbstractEntity<?> critOnlySinglePrototype;
    private final IDates dates;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Inject
    public EntityQueryCriteria(final IValueMatcherFactory valueMatcherFactory, final IGeneratedEntityController generatedEntityController, final ISerialiser serialiser, final ICompanionObjectFinder controllerProvider, final IDates dates) {
        this.generatedEntityController = generatedEntityController;
        this.serialiser = serialiser;
        this.controllerProvider = controllerProvider;
        this.dates = dates;
        
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

    @Deprecated
    public IValueMatcher<?> getValueMatcher(final String propertyName) {
        throw new UnsupportedOperationException("This is an old Swing related code, which should not be called.");
        //        if (valueMatchers.get(propertyName) == null) {
        //            valueMatchers.put(propertyName, valueMatcherFactory.getValueMatcher(getType(), propertyName));
        //        }
        //        return valueMatchers.get(propertyName);
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
     * Enhances this criteria entity with fetch provider consisting from properties that are used for tooltips of other properties; it will extend the fetching strategy of running queries on top of chosen result-set properties.
     *
     * @param additionalFetchProviderForTooltipProperties
     */
    public void setAdditionalFetchProviderForTooltipProperties(final IFetchProvider<T> additionalFetchProviderForTooltipProperties) {
        this.additionalFetchProviderForTooltipProperties = Optional.of(additionalFetchProviderForTooltipProperties);
    }

    public void setCreatedByUserConstraint(final User user) {
        this.createdByUserConstraint = Optional.of(user);
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
    private static <T extends AbstractEntity<?>> EntityResultQueryModel<T> createQuery(final Class<T> managedType, final List<QueryProperty> queryProperties, final Optional<IQueryEnhancer<T>> additionalQueryEnhancer, final Optional<CentreContext<T, ?>> centreContextForQueryEnhancer, final Optional<User> createdByUserConstraint, final IDates dates) {
        final EntityResultQueryModel<T> query = createCompletedQuery(managedType, queryProperties, additionalQueryEnhancer, centreContextForQueryEnhancer, createdByUserConstraint, dates).model();
        query.setFilterable(true);
        return query;
    }

    private static <T extends AbstractEntity<?>> ICompleted<T> createCompletedQuery (final Class<T> managedType, final List<QueryProperty> queryProperties, final Optional<IQueryEnhancer<T>> additionalQueryEnhancer, final Optional<CentreContext<T, ?>> centreContextForQueryEnhancer, final Optional<User> createdByUserConstraint, final IDates dates) {
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

    /**
     * Generates an EQL model to the stage of {@link ICompleted}, which can be conveniently complemented with custom yield instructions.
     *
     * @return
     */
    public ICompleted<T> createQuery() {
        return createCompletedQuery(getManagedType(), createQueryProperties(), additionalQueryEnhancer, centreContextForQueryEnhancer, createdByUserConstraint, dates);
    }

    /**
     * Returns the parameter map for query.
     *
     * @return
     */
    public Map<String, Object> getParameters () {
        final IAddToCriteriaTickManager criteriaTickManager = getCentreDomainTreeMangerAndEnhancer().getFirstTick();
        final Map<String, Pair<Object, Object>> paramMap = createParamValuesMap(getEntityClass(), getManagedType(), criteriaTickManager, dates);
        final Map<String, Object> resultMap = enhanceQueryParams(buildParametersMap(getManagedType(), paramMap));
        resultMap.putAll(getQueryPropertyParameters());
        return resultMap;
    }

    /**
     * Populates parameters map with instances of {@link QueryProperty}.
     * This method should be used to expand mnemonics value into conditions for the EQL {@code critCondition} operator.
     *
     * @return
     */
    private Map<String, QueryProperty> getQueryPropertyParameters() {
        return getCentreDomainTreeMangerAndEnhancer().getFirstTick().checkedProperties(getEntityClass()).stream()
                .filter(propName -> !isPlaceholder(propName))
                .map(propName -> createQueryProperty(propName))
                .filter(qp -> qp.isCritOnly() || qp.isAECritOnlyChild())
                .collect(toMap(qp -> queryPropertyParamName(qp.getPropertyName()), qp -> qp));
    }
    /**
     * Creates the fetch provider based on 'properties' of the 'managedType' and on custom fetch provider (if any).
     * <p>
     * Please, note that custom fetch provider can have its top-level type marked as 'instrumented' (or its property fetch providers). If this is the case, then it will override
     * existing defaults (for top-level entity it will override .lightweight on query builder and for properties it will override their concrete fetch models).
     *
     * @param managedType
     * @param properties
     * @param additionalFetchProvider
     * @param additionalFetchProviderForTooltipProperties
     *
     * @return
     */
    protected static <T extends AbstractEntity<?>, V extends AbstractEntity<?>> IFetchProvider<V> createFetchModelFrom(final Class<V> managedType, final Set<String> properties, final Optional<IFetchProvider<T>> additionalFetchProvider, final Optional<IFetchProvider<T>> additionalFetchProviderForTooltipProperties) {
        final IFetchProvider<V> rootProvider = properties.contains("") ? EntityUtils.fetchNotInstrumentedWithKeyAndDesc(managedType)
                : EntityUtils.fetchNotInstrumented(managedType);
        // Analyse 'properties' and get all 'short collectional' properties if any. Then extend main fetch provider with key-fetched providers of short collection parents.
        // This should be done in order to be able to correctly retrieve centre results with short collections (EQL retrieval sorts collection elements by keys and thus such enhancement is required).
        final IFetchProvider<V> rootProviderWithResultSetProperties = properties.stream()
                .filter(property -> isShortCollection(managedType, property))
                .reduce(rootProvider.with(properties), (fetchProvider, shortCollectionProp) -> {
                    final String shortCollectionParent = isDotNotation(shortCollectionProp) ? penultAndLast(shortCollectionProp).getKey() : "";
                    final Class<AbstractEntity<?>> shortCollectionParentType = (Class<AbstractEntity<?>>) transform(managedType, shortCollectionProp).getKey();
                    return "".equals(shortCollectionParent)
                            ? fetchProvider.with((IFetchProvider<V>) EntityUtils.fetchNotInstrumentedWithKeyAndDesc(shortCollectionParentType))
                            : fetchProvider.with(shortCollectionParent, EntityUtils.fetchNotInstrumentedWithKeyAndDesc(shortCollectionParentType));
                }, (fetchProvider1, fetchProvider2) -> fetchProvider1.with(fetchProvider2));
        final IFetchProvider<V> resultWithoutTooltipProps = additionalFetchProvider
            .map(value -> rootProviderWithResultSetProperties.with(value.copy(managedType)))
            .orElse(rootProviderWithResultSetProperties);
        return additionalFetchProviderForTooltipProperties
            .map(value -> resultWithoutTooltipProps.with(value.copy(managedType)))
            .orElse(resultWithoutTooltipProps);
    }

    /**
     * This is temporary solution needed for pagination support on web ui
     */
    public IPage<T> getPage(final int pageNumber, final int pageCount, final int pageCapacity) {
        final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = generateQuery();
        if (getManagedType().equals(getEntityClass())) {
            return dao.getPage(resultQuery, pageNumber, pageCount, pageCapacity);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.getPage(resultQuery, pageNumber, pageCount, pageCapacity, getByteArrayForManagedType());
        }
    }

    /**
     * Returns the data page with summary/ Before it retrieves the page it also calculates whether specified page number is in the range available pages.
     *
     * @param pageNumber
     * @param pageCapacity
     * @return
     */
    public Pair<IPage<T>, T> getPageWithSummaries(final int pageNumber, final int pageCapacity) {
        final Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> resultQuery = generateQueryWithSummaries();
        final int entitiesCount = count(resultQuery.getKey());
        final int pageSize = entitiesCount % pageCapacity == 0 ? entitiesCount / pageCapacity : entitiesCount / pageCapacity + 1;
        if (pageSize == 0) {
            return new Pair<>(new EmptyPage<T>(), null);
        } else if (pageSize <= pageNumber) {
            return new Pair<>(getPage(resultQuery.getKey(), pageSize - 1, pageCapacity), getSummary(resultQuery.getValue()));
        }
        return new Pair<>(getPage(resultQuery.getKey(), pageNumber, pageCapacity), getSummary(resultQuery.getValue()));
    }

    /**
     * Calculates the summaries for specified query.
     *
     * @param query
     * @return
     */
    private T getSummary(final QueryExecutionModel<T, EntityResultQueryModel<T>> query) {
        if (query != null) {
            final List<T> list = getAllEntities(query);
            return list.size() == 1 ? list.get(0) : null;
        }
        return null;
    }

    private IPage<T> getPage(final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery, final int pageNumber, final int pageCapacity) {
        if (getManagedType().equals(getEntityClass())) {
            return dao.getPage(resultQuery, pageNumber, pageCapacity);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.getPage(resultQuery, pageNumber, pageCapacity, getByteArrayForManagedType());
        }
    }

    /**
     * Generates the query and summary query for this entity query criteria.
     *
     * @return
     */
    private Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> generateQueryWithSummaries() {
        final Class<?> root = getEntityClass();
        final IAddToResultTickManager resultTickManager = getCentreDomainTreeMangerAndEnhancer().getSecondTick();
        final IDomainTreeEnhancer enhancer = getCentreDomainTreeMangerAndEnhancer().getEnhancer();
        final Pair<Set<String>, Set<String>> separatedFetch = EntityQueryCriteriaUtils.separateFetchAndTotalProperties(root, resultTickManager, enhancer);
        final Map<String, Object> paramMap = getParameters();
        final EntityResultQueryModel<T> notOrderedQuery = createQuery(getManagedType(), createQueryProperties(), additionalQueryEnhancer, centreContextForQueryEnhancer, createdByUserConstraint, dates);
        final IFetchProvider<T> fetchProvider = createFetchModelFrom(getManagedType(), separatedFetch.getKey(), additionalFetchProvider, additionalFetchProviderForTooltipProperties);
        final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = adjustLightweightness(notOrderedQuery, fetchProvider.instrumented())
                .with(DynamicOrderingBuilder.createOrderingModel(getManagedType(), resultTickManager.orderedProperties(root)))//
                .with(fetchProvider.fetchModel())//
                .with(paramMap).model();
        if (!separatedFetch.getValue().isEmpty()) {
            final QueryExecutionModel<T, EntityResultQueryModel<T>> totalQuery = adjustLightweightness(notOrderedQuery, false)
                    .with(DynamicFetchBuilder.createTotalFetchModel(getManagedType(), separatedFetch.getValue()))//
                    .with(paramMap).model();
            return new Pair<>(resultQuery, totalQuery);
        }
        return new Pair<>(resultQuery, null);
    }

    private int count(final QueryExecutionModel<T, EntityResultQueryModel<T>> query) {
        return dao.count(query.getQueryModel(), query.getParamValues());
    }

    /**
     * This is temporary solution needed for pagination support on web ui
     */
    public IPage<T> getPage(final int pageNumber, final int pageCapacity) {
        final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = generateQuery();
        if (getManagedType().equals(getEntityClass())) {
            return dao.getPage(resultQuery, pageNumber, pageCapacity);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.getPage(resultQuery, pageNumber, pageCapacity, getByteArrayForManagedType());
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
        final Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> queries = generateQueryWithSummaries();
        if (queries.getValue() != null) {
            return firstPage(queries.getKey(), queries.getValue(), pageSize);
        } else {
            return run(queries.getKey(), pageSize);
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
        final IAddToResultTickManager tickManager = getCentreDomainTreeMangerAndEnhancer().getSecondTick();
        final IDomainTreeEnhancer enhancer = getCentreDomainTreeMangerAndEnhancer().getEnhancer();
        final Pair<Set<String>, Set<String>> separatedFetch = EntityQueryCriteriaUtils.separateFetchAndTotalProperties(root, tickManager, enhancer);
        final EntityResultQueryModel<T> notOrderedQuery = createQuery(getManagedType(), createQueryProperties(), additionalQueryEnhancer, centreContextForQueryEnhancer, createdByUserConstraint, dates);
        final IFetchProvider<T> fetchProvider = createFetchModelFrom(getManagedType(), separatedFetch.getKey(), additionalFetchProvider, additionalFetchProviderForTooltipProperties);
        final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = adjustLightweightness(notOrderedQuery, fetchProvider.instrumented())
                .with(DynamicOrderingBuilder.createOrderingModel(getManagedType(), tickManager.orderedProperties(root)))//
                .with(fetchProvider.fetchModel())//
                .with(getParameters()).model();
        export(fileName, resultQuery, propertyNames, propertyTitles);
    }

    private QueryExecutionModel.Builder<T, EntityResultQueryModel<T>> adjustLightweightness(final EntityResultQueryModel<T> notOrderedQuery, final boolean shouldBeInstrumented) {
        // all import row centre should be heavyweight to populate errors trough ACE handlers
        return shouldBeInstrumented ? from(notOrderedQuery) : from(notOrderedQuery).lightweight();
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
    public void export(final String fileName, final QueryExecutionModel<T, ?> qem, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        final byte[] content;
        final QueryExecutionModel<T, ?> query = qem.lightweight();
        if (getManagedType().equals(getEntityClass())) {
            content = dao.export(query, propertyNames, propertyTitles);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            content = generatedEntityController.export(query, propertyNames, propertyTitles, getByteArrayForManagedType());
        }
        try (final FileOutputStream fo = new FileOutputStream(fileName)) {
            fo.write(content);
            fo.flush();
        }
    }

    private QueryExecutionModel<T, EntityResultQueryModel<T>> generateQuery() {
        final Class<?> root = getEntityClass();
        final IAddToResultTickManager resultTickManager = getCentreDomainTreeMangerAndEnhancer().getSecondTick();
        final IDomainTreeEnhancer enhancer = getCentreDomainTreeMangerAndEnhancer().getEnhancer();
        final Pair<Set<String>, Set<String>> separatedFetch = EntityQueryCriteriaUtils.separateFetchAndTotalProperties(root, resultTickManager, enhancer);
        final EntityResultQueryModel<T> notOrderedQuery = createQuery(getManagedType(), createQueryProperties(), additionalQueryEnhancer, centreContextForQueryEnhancer, createdByUserConstraint, dates);
        final IFetchProvider<T> fetchProvider = createFetchModelFrom(getManagedType(), separatedFetch.getKey(), additionalFetchProvider, additionalFetchProviderForTooltipProperties);
        return adjustLightweightness(notOrderedQuery, fetchProvider.instrumented())
                .with(DynamicOrderingBuilder.createOrderingModel(getManagedType(), resultTickManager.orderedProperties(root)))//
                .with(fetchProvider.fetchModel())//
                .with(getParameters()).model();
    }

    public Pair<String[], String[]> generatePropTitlesToExport() {
        final Class<?> root = getEntityClass();
        final IAddToResultTickManager tickManager = getCentreDomainTreeMangerAndEnhancer().getSecondTick();
        final List<String> propertyNames = new ArrayList<>();
        final List<String> propertyTitles = new ArrayList<>();
        for (final String propertyName : tickManager.usedProperties(root)) {
            if (tickManager.getWidth(root, propertyName) > 0) {
                propertyNames.add(propertyName);
                propertyTitles.add(CriteriaReflector.getCriteriaTitleAndDesc(getManagedType(), propertyName).getKey());
            }
        }
        return new Pair<>(propertyNames.toArray(new String[] {}), propertyTitles.toArray(new String[] {}));
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
        final fetch<T> fetchModel = createFetchModelFrom(getManagedType(), separatedFetch.getKey(), additionalFetchProvider, additionalFetchProviderForTooltipProperties).fetchModel();
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
        } else if (DynamicEntityClassLoader.isGenerated(entity.getType()) && DynamicEntityClassLoader.getOriginalType(entity.getType()).equals(getEntityClass())) {
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
        final List<QueryProperty> queryProperties = new ArrayList<>();
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
     * @return
     */
    public final List<T> getAllEntities() {
        return getAllEntities(generateQuery());
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
     * Returns a stream of entities that match the generated query.
     * The returned stream must always be wrapped into <code>try with resources</code> clause to ensure that the underlying resultset is closed.
     */

    public final Stream<T> streamEntities(final int fetchSize, final Long... ids) {
        return streamEntities(generateQuery(), fetchSize, ids);
    }

    /**
     * Returns a stream of entities that match the provided query.
     * The returned stream must always be wrapped into <code>try with resources</code> clause to ensure that the underlying resultset is closed.
     */
    public final Stream<T> streamEntities(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final int fetchSize, final Long... ids) {

        final QueryExecutionModel<T, EntityResultQueryModel<T>> qem;
        if (ids.length == 0) {
            qem = queryModel;
        } else {
            final EntityResultQueryModel<T> queryWithIds = select(getManagedType())
                    .where().prop("id").in().values(ids)
                    .model();
            qem = from(queryWithIds).with(queryModel.getFetchModel()).with(queryModel.getOrderModel()).with(queryModel.getParamValues()).lightweight().model();
        }

        if (getManagedType().equals(getEntityClass())) {
            return dao.stream(qem, fetchSize);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.stream(qem, fetchSize);
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
        if (isDoubleCriterion(getManagedType(), actualProperty)) {
            queryProperty.setValue2(tickManager.getValue2(root, actualProperty));
        }
        if (isDoubleCriterion(getManagedType(), actualProperty) && !isBooleanCriterion(getManagedType(), actualProperty)) {
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
        queryProperty.setOrGroup(tickManager.getOrGroup(root, actualProperty));
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
        final List<byte[]> byteArray = new ArrayList<>(list.size());
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

    /**
     * Initialises crit-only single prototype entity if it was not initialised before. Returns it as a result.
     *
     * @param entityType -- the type of the prototype (only for initialisation)
     * @param id -- id for the prototype (only for initialisation)
     * @return
     */
    public AbstractEntity<?> critOnlySinglePrototypeInit(final Class<AbstractEntity<?>> entityType, final Long id) {
        if (critOnlySinglePrototype == null) {
            critOnlySinglePrototype = getEntityFactory().newEntity(entityType, id);
            critOnlySinglePrototype.resetMetaState();
            // Initialisation phase is started here, so that definers will be actioned with isInitialising = true mark. Validation will be deferred to the moment when initialisation phase ends.
            critOnlySinglePrototype.beginInitialising();
        }
        return critOnlySinglePrototype();
    }

    /**
     * Returns crit-only single prototype entity.
     *
     * @return
     */
    public AbstractEntity<?> critOnlySinglePrototype() {
        return critOnlySinglePrototype;
    }

    /**
     * Returns crit-only single prototype entity if exists.
     *
     * @return
     */
    public Optional<AbstractEntity<?>> critOnlySinglePrototypeOptional() {
        return ofNullable(critOnlySinglePrototype());
    }

    public void setDynamicProperties (final List<List<DynamicColumnForExport>> dynamicProperties) {
        this.dynamicProperties.clear();
        this.dynamicProperties.addAll(dynamicProperties);
    }

    public List<List<DynamicColumnForExport>> getDynamicProperties() {
        return Collections.unmodifiableList(dynamicProperties);
    }

}