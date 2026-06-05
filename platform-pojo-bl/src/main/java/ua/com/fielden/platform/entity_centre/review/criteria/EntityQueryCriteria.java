package ua.com.fielden.platform.entity_centre.review.criteria;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
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
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicFetchBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicOrderingBuilder;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.*;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isShortCollection;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity_centre.review.DynamicParamBuilder.buildParametersMap;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty.queryPropertyParamName;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteriaUtils.*;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.*;
import static ua.com.fielden.platform.utils.Lazy.lazySupplier;
import static ua.com.fielden.platform.utils.Pair.pair;

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
    public final Supplier<List<QueryProperty>> queryProperties;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Inject
    public EntityQueryCriteria(final IGeneratedEntityController generatedEntityController, final ISerialiser serialiser, final ICompanionObjectFinder controllerProvider, final IDates dates) {
        this.generatedEntityController = generatedEntityController;
        this.serialiser = serialiser;
        this.controllerProvider = controllerProvider;
        this.dates = dates;
        this.queryProperties = lazySupplier(this::createQueryProperties);

        // These values are intended to be initialised via reflection.
        this.dao = null;
        this.cdtme = null;
    }

    public DAO companionObject() {
        return dao;
    }

    /// Returns the centre’s domain tree manager.
    ///
    public C getCentreDomainTreeMangerAndEnhancer() {
        return cdtme;
    }

    /// Returns the root entity for which this criterion was generated.
    ///
    public Class<T> getEntityClass() {
        return dao.getEntityType();
    }

    /// Returns the enhanced type for the entity class.
    /// The entity class is obtained using the [#getEntityClass()] method.
    ///
    @SuppressWarnings("unchecked")
    public Class<T> getManagedType() {
        return (Class<T>) getCentreDomainTreeMangerAndEnhancer().getEnhancer().getManagedType(getEntityClass());
    }

    /// Loads the default values for the properties of the binding entity.
    ///
    public void defaultValues() {
        final IAddToCriteriaTickRepresentation ftr = getCentreDomainTreeMangerAndEnhancer().getRepresentation().getFirstTick();
        for (final Field propertyField : CriteriaReflector.getCriteriaProperties(getType())) {
            final SecondParam secondParam = AnnotationReflector.getAnnotation(propertyField, SecondParam.class);
            final CriteriaProperty critProperty = AnnotationReflector.getAnnotation(propertyField, CriteriaProperty.class);
            final Class<T> root = getEntityClass();
            set(propertyField.getName(), secondParam == null ? ftr.getValueByDefault(root, critProperty.propertyName()) : ftr.getValue2ByDefault(root, critProperty.propertyName()));
        }
    }

    /// Enhances this criteria entity with a custom fetch provider,
    /// which extends the fetching strategy for queries based on the selected result-set properties.
    ///
    public void setAdditionalFetchProvider(final IFetchProvider<T> additionalFetchProvider) {
        this.additionalFetchProvider = Optional.of(additionalFetchProvider);
    }

    /// Enhances this criteria entity with a fetch provider consisting of properties, used as tooltips for other properties.
    /// This fetch provider extends the fetching strategy for queries based on the selected result-set properties.
    ///
    public void setAdditionalFetchProviderForTooltipProperties(final IFetchProvider<T> additionalFetchProviderForTooltipProperties) {
        this.additionalFetchProviderForTooltipProperties = Optional.of(additionalFetchProviderForTooltipProperties);
    }

    public void setCreatedByUserConstraint(final User user) {
        this.createdByUserConstraint = Optional.of(user);
    }

    /// Enhances this criteria entity with a custom query enhancer and its optional centre context,
    /// which extends the query based on the selected criteria conditions.
    ///
    public void setAdditionalQueryEnhancerAndContext(final IQueryEnhancer<T> additionalQueryEnhancer, final Optional<CentreContext<T, ?>> centreContextForQueryEnhancer) {
        this.additionalQueryEnhancer = Optional.of(additionalQueryEnhancer);
        this.centreContextForQueryEnhancer = centreContextForQueryEnhancer;
    }

    /// Creates a query model based on the `queryProperties` of the `managedType`
    /// and an optional custom query enhancer.
    ///
    private static <T extends AbstractEntity<?>> EntityResultQueryModel<T> createQuery(final Class<T> type, final Class<T> managedType, final List<QueryProperty> queryProperties, final Optional<IQueryEnhancer<T>> additionalQueryEnhancer, final Optional<CentreContext<T, ?>> centreContextForQueryEnhancer, final Optional<User> createdByUserConstraint, final IDates dates) {
        final EntityResultQueryModel<T> query = createCompletedQuery(type, managedType, queryProperties, additionalQueryEnhancer, centreContextForQueryEnhancer, createdByUserConstraint, dates).model();
        query.setFilterable(true);
        return query;
    }

    /// Generates an EQL model up to the [ICompleted] stage,
    /// which can then be conveniently extended with custom yield instructions.
    ///
    public ICompleted<T> createQuery() {
        return createCompletedQuery(getEntityClass(), getManagedType(), queryProperties.get(), additionalQueryEnhancer, centreContextForQueryEnhancer, createdByUserConstraint, dates);
    }

    /// Returns the parameter map for the query.
    ///
    public Map<String, Object> getParameters () {
        final IAddToCriteriaTickManager criteriaTickManager = getCentreDomainTreeMangerAndEnhancer().getFirstTick();
        final Map<String, Pair<Object, Object>> paramMap = createParamValuesMap(getEntityClass(), getManagedType(), criteriaTickManager, dates);
        final Map<String, Object> resultMap = enhanceQueryParams(buildParametersMap(getManagedType(), paramMap));
        resultMap.putAll(getQueryPropertyParameters());
        return resultMap;
    }

    /// Populates the parameter map with instances of [QueryProperty].
    /// This method should be used to expand mnemonic into conditions for the EQL `critCondition` operator.
    ///
    private Map<String, QueryProperty> getQueryPropertyParameters() {
        return getCentreDomainTreeMangerAndEnhancer().getFirstTick().checkedProperties(getEntityClass()).stream()
                .filter(propName -> !isPlaceholder(propName))
                .map(propName -> createQueryProperty(propName))
                .filter(qp -> qp.isCritOnly() || qp.isAECritOnlyChild())
                .collect(toMap(qp -> queryPropertyParamName(qp.getPropertyName()), qp -> qp));
    }

    /// Creates a fetch provider based on the `properties` of the `managedType`
    /// and an optional custom fetch provider.
    ///
    /// Note that a custom fetch provider may have its top-level type (or its property fetch providers)
    /// marked as *instrumented*.
    /// In such cases, it will override the existing defaults —
    /// for the top-level entity, it overrides `.lightweight` in the query builder,
    /// and for properties, it overrides their specific fetch models.
    ///
    protected static <T extends AbstractEntity<?>, V extends AbstractEntity<?>> IFetchProvider<V>
    createFetchModelFrom(
            final Class<V> managedType, final Set<String> properties,
            final Optional<IFetchProvider<T>> additionalFetchProvider,
            final Optional<IFetchProvider<T>> additionalFetchProviderForTooltipProperties)
    {
        final IFetchProvider<V> rootProvider = properties.contains("")
                                               ? EntityUtils.fetchNotInstrumentedWithKeyAndDesc(managedType)
                                               : EntityUtils.fetchNotInstrumented(managedType);
        final Set<String> authorisedProperties = properties.stream().filter(prop -> isPropertyAuthorised(managedType, prop)).collect(toCollection(LinkedHashSet::new));
        // Analyse `properties` and collect all "short collectional" properties, if any.
        // Then extend the main fetch provider with key-fetched providers of the short collection parents.
        // This is necessary to correctly retrieve centre results containing short collections,
        // since EQL retrieval sorts collection elements by keys, making this enhancement required.
        final IFetchProvider<V> rootProviderWithResultSetProperties = authorisedProperties.stream()
                .filter(property -> isShortCollection(managedType, property))
                .reduce(rootProvider.with(authorisedProperties), (fetchProvider, shortCollectionProp) -> {
                    final String shortCollectionParent = isDotExpression(shortCollectionProp) ? penultAndLast(shortCollectionProp).getKey() : "";
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

    /// Temporary solution required to support pagination in the Web UI.
    ///
    @Deprecated(forRemoval = true, since = "2.2.0")
    public IPage<T> getPage(final int pageNumber, final int pageCount, final int pageCapacity) {
        final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = generateQuery();
        if (getManagedType().equals(getEntityClass())) {
            return dao.getPage(resultQuery, pageNumber, pageCount, pageCapacity);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.getPage(resultQuery, pageNumber, pageCount, pageCapacity);
        }
    }

    /// Returns a data page including summary information.
    /// Before retrieving the page, it checks whether the specified page number is within the range of available pages.
    ///
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

    /// Calculates the summaries for the given `query`.
    ///
    private T getSummary(final QueryExecutionModel<T, EntityResultQueryModel<T>> query) {
        if (query != null) {
            final List<T> list = getAllEntities(query);
            return list.size() == 1 ? list.getFirst() : null;
        }
        return null;
    }

    private IPage<T> getPage(final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery, final int pageNumber, final int pageCapacity) {
        if (getManagedType().equals(getEntityClass())) {
            return dao.getPage(resultQuery, pageNumber, pageCapacity);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.getPage(resultQuery, pageNumber, pageCapacity);
        }
    }

    /// Generates a query and a corresponding summary query for this entity query criteria.
    ///
    private Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> generateQueryWithSummaries() {
        final Class<?> root = getEntityClass();
        final IAddToResultTickManager resultTickManager = getCentreDomainTreeMangerAndEnhancer().getSecondTick();
        final IDomainTreeEnhancer enhancer = getCentreDomainTreeMangerAndEnhancer().getEnhancer();
        final Pair<Set<String>, Set<String>> separatedFetch = separateFetchAndTotalProperties(root, resultTickManager, enhancer);
        final Map<String, Object> paramMap = getParameters();
        final EntityResultQueryModel<T> notOrderedQuery = createQuery(getEntityClass(), getManagedType(), queryProperties.get(), additionalQueryEnhancer, centreContextForQueryEnhancer, createdByUserConstraint, dates);
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

    /// Temporary solution required to support pagination in the Web UI.
    ///
    public IPage<T> getPage(final int pageNumber, final int pageCapacity) {
        final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = generateQuery();
        if (getManagedType().equals(getEntityClass())) {
            return dao.getPage(resultQuery, pageNumber, pageCapacity);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.getPage(resultQuery, pageNumber, pageCapacity);
        }
    }

    private Map<String, Object> enhanceQueryParams(final Map<String, Object> buildParametersMap) {
        if (additionalQueryEnhancer.isPresent()) {
            return additionalQueryEnhancer.get().enhanceQueryParams(buildParametersMap, centreContextForQueryEnhancer);
        }
        return buildParametersMap;
    }

    /// Executes the configured entity query.
    ///
    public final IPage<T> run(final int pageSize) {
        final Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> queries = generateQueryWithSummaries();
        if (queries.getValue() != null) {
            return firstPage(queries.getKey(), queries.getValue(), pageSize);
        } else {
            return run(queries.getKey(), pageSize);
        }
    }

    /// Executes the specified query and returns its results.
    ///
    public final IPage<T> run(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final int pageSize) {
        return firstPage(queryModel, pageSize);
    }

    /// Executes the specified query model and returns the resulting list.
    ///
    public final List<T> run(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel) {
        return getAllEntities(queryModel);
    }

    /// Executes the specified query model using `managedType` and its byte representation.
    /// Returns a result page of the specified size.
    ///
    public final IPage<T> run(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final Class<T> managedType, final byte[] managedTypeArray, final int pageSize) {
        generatedEntityController.setEntityType(managedType);
        return generatedEntityController.firstPage(queryModel, pageSize);
    }

    /// Executes the specified query model using `managedType` and its byte representation.
    ///
    public final List<T> run(final ICentreDomainTreeManagerAndEnhancer cdtmeWithWhichAnalysesQueryHaveBeenCreated, final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final Class<T> managedType, final byte[] managedTypeArray) {
        generatedEntityController.setEntityType(managedType);
        // TODO here cdtme from this instance is magically DIFFERENT from cdtme, on which query has been created. So byte arrays will be taken from original Cdtme. Should that Cdtmes be the same??
        // TODO here cdtme from this instance is magically DIFFERENT from cdtme, on which query has been created. So byte arrays will be taken from original Cdtme. Should that Cdtmes be the same??
        // TODO here cdtme from this instance is magically DIFFERENT from cdtme, on which query has been created. So byte arrays will be taken from original Cdtme. Should that Cdtmes be the same??
        return generatedEntityController.getAllEntities(queryModel);
    }

    /// Returns a [LifecycleModel] instance corresponding to `model`,
    /// the lifecycle property `propertyName`, and the date interval `from`/`to`.
    /// Returns `null` if the associated controller is not lifecycle-aware.
    ///
    @SuppressWarnings("unchecked")
    public final LifecycleModel<T> getLifecycleInformation(final EntityResultQueryModel<? extends AbstractEntity<?>> model, final List<String> distributionProperties, final String propertyName, final DateTime from, final DateTime to) {
        if (isLifecycleController()) {
            return ((ILifecycleDao<T>) dao).getLifecycleInformation(model, distributionProperties, propertyName, from, to);
        } else {
            return null;
        }
    }

    /// Returns whether the companion object associated with `this` criteria is lifecycle-aware.
    ///
    public boolean isLifecycleController() {
        return dao instanceof ILifecycleDao;
    }

    /// Exports data to the specified external file.
    ///
    @Deprecated(forRemoval = true, since = "2.2.0")
    public void export(final String fileName, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        final Class<?> root = getEntityClass();
        final IAddToResultTickManager tickManager = getCentreDomainTreeMangerAndEnhancer().getSecondTick();
        final IDomainTreeEnhancer enhancer = getCentreDomainTreeMangerAndEnhancer().getEnhancer();
        final Pair<Set<String>, Set<String>> separatedFetch = separateFetchAndTotalProperties(root, tickManager, enhancer);
        final EntityResultQueryModel<T> notOrderedQuery = createQuery(getEntityClass(), getManagedType(), queryProperties.get(), additionalQueryEnhancer, centreContextForQueryEnhancer, createdByUserConstraint, dates);
        final IFetchProvider<T> fetchProvider = createFetchModelFrom(getManagedType(), separatedFetch.getKey(), additionalFetchProvider, additionalFetchProviderForTooltipProperties);
        final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = adjustLightweightness(notOrderedQuery, fetchProvider.instrumented())
                .with(DynamicOrderingBuilder.createOrderingModel(getManagedType(), tickManager.orderedProperties(root)))//
                .with(fetchProvider.fetchModel())//
                .with(getParameters()).model();
        export(fileName, resultQuery, propertyNames, propertyTitles);
    }

    private QueryExecutionModel.Builder<T, EntityResultQueryModel<T>> adjustLightweightness(final EntityResultQueryModel<T> notOrderedQuery, final boolean shouldBeInstrumented) {
        // All import row centres should be heavyweight to populate errors through ACE handlers.
        return shouldBeInstrumented ? from(notOrderedQuery) : from(notOrderedQuery).lightweight();
    }

    /// Exports the data retrieved using `qem` to a file named `filename`.
    ///
    public void export(final String fileName, final QueryExecutionModel<T, ?> qem, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        final byte[] content;
        final QueryExecutionModel<T, ?> query = qem.lightweight();
        if (getManagedType().equals(getEntityClass())) {
            content = dao.export(query, propertyNames, propertyTitles);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            content = generatedEntityController.export(query, propertyNames, propertyTitles);
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
        final Pair<Set<String>, Set<String>> separatedFetch = separateFetchAndTotalProperties(root, resultTickManager, enhancer);
        final EntityResultQueryModel<T> notOrderedQuery = createQuery(getEntityClass(), getManagedType(), queryProperties.get(), additionalQueryEnhancer, centreContextForQueryEnhancer, createdByUserConstraint, dates);
        final IFetchProvider<T> fetchProvider = createFetchModelFrom(getManagedType(), separatedFetch.getKey(), additionalFetchProvider, additionalFetchProviderForTooltipProperties);
        return adjustLightweightness(notOrderedQuery, fetchProvider.instrumented())
                .with(DynamicOrderingBuilder.createOrderingModel(getManagedType(), resultTickManager.orderedProperties(root)))//
                .with(fetchProvider.fetchModel())//
                .with(getParameters()).model();
    }

    /// Creates an [IFetchProvider] for the entity centre’s result-set.
    /// The result includes all properties from the result-set columns, as well as those from an additional fetch provider, if specified.
    /// It also includes any properties used as tooltips for other properties.
    ///
    public IFetchProvider<T> createResultSetFetchProvider() {
        return createFetchModelFrom(
            getManagedType(),
            separateFetchAndTotalProperties(
                getEntityClass(),
                getCentreDomainTreeMangerAndEnhancer().getSecondTick(),
                getCentreDomainTreeMangerAndEnhancer().getEnhancer()
            ).getKey(),
            additionalFetchProvider,
            additionalFetchProviderForTooltipProperties
        );
    }

    public Pair<String[], String[]> generatePropTitlesToExport() {
        final Class<?> root = getEntityClass();
        final IAddToResultTickManager tickManager = getCentreDomainTreeMangerAndEnhancer().getSecondTick();
        final List<String> propertyNames = new ArrayList<>();
        final List<String> propertyTitles = new ArrayList<>();
        for (final String propertyName : tickManager.usedProperties(root)) {
            if (isPropertyAuthorised(root, propertyName) && tickManager.getWidth(root, propertyName) > 0) {
                propertyNames.add(propertyName);
                propertyTitles.add(CriteriaReflector.getCriteriaTitleAndDesc(getManagedType(), propertyName).getKey());
            }
        }
        return new Pair<>(propertyNames.toArray(new String[] {}), propertyTitles.toArray(new String[] {}));
    }

    /// Returns an entity instance by its `id` or `null` if none found.
    ///
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
            return generatedEntityController.findById(id, fetchModel);
        }
    }

    /// Removes the given `entity`.
    /// Throws an exception if the `entity` is not of the entity type or managed type associated with `this` criteria.
    ///
    public void delete(final T entity) {
        if (entity.getType().equals(getEntityClass())) {
            dao.delete(entity);
        } else {
            throw new IllegalArgumentException("Unexpected entity type " + entity.getType() + " preventing deletion.");
        }
    }

    /// Returns the `entity` refetched using the specified `fetchModel`.
    ///
    public T refetchEntity(final T entity, final fetch<T> fetchModel) {
        if (entity.getType().equals(getEntityClass())) {
            return dao.findByEntityAndFetch(fetchModel, entity);
        } else if (DynamicEntityClassLoader.isGenerated(entity.getType()) && DynamicEntityClassLoader.getOriginalType(entity.getType()).equals(getEntityClass())) {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.findById(entity.getId(), fetchModel);
        }
        throw new IllegalArgumentException("The entity type is incorrect. The entity type must be: " + getEntityClass() + " but was "
                + DynamicEntityClassLoader.getOriginalType(entity.getType()).equals(getEntityClass()));
    }

    /// Converts existing properties model (which has separate properties for `from`/`to`, `is`/`isNot`, and so on) into a new properties model,
    /// with a separate abstraction for every criterion.
    ///
    public final List<QueryProperty> createQueryProperties() {
        final List<QueryProperty> queryProperties = new ArrayList<>();
        for (final String actualProperty : getCentreDomainTreeMangerAndEnhancer().getFirstTick().checkedProperties(getEntityClass())) {
            if (!AbstractDomainTree.isPlaceholder(actualProperty)) {
                queryProperties.add(createQueryProperty(actualProperty));
            }
        }
        return queryProperties;
    }

    /// Returns the first result page for the given query model.
    ///
    /// @param queryModel the query model for which the first result page should be returned
    /// @param pageSize   the size of the page
    ///
    public final IPage<T> firstPage(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final int pageSize) {
        if (getManagedType().equals(getEntityClass())) {
            return dao.firstPage(queryModel, pageSize);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.firstPage(queryModel, pageSize);
        }
    }

    /// Returns all entities that satisfy conditions of `this` entity query criteria.
    ///
    @Deprecated(forRemoval = true, since = "2.2.0")
    public final List<T> getAllEntities() {
        return getAllEntities(generateQuery());
    }

    /// Returns all entities that satisfy conditions of `this` entity query criteria with summary.
    ///
    public final Pair<List<T>, T> getAllEntitiesWithSummary() {
        final Pair<QueryExecutionModel<T, EntityResultQueryModel<T>>, QueryExecutionModel<T, EntityResultQueryModel<T>>> queries = generateQueryWithSummaries();
        final List<T> entities = getAllEntities(queries.getKey());
        return pair(entities, entities.isEmpty() ? null : getSummary(queries.getValue()));
    }

    /// Returns all entities retrieved by the given `queryModel`.
    ///
    /// @param queryModel the query model used to fetch the entities
    ///
    public final List<T> getAllEntities(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel) {
        if (getManagedType().equals(getEntityClass())) {
            return dao.getAllEntities(queryModel);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.getAllEntities(queryModel);
        }
    }

    /// Returns a stream of entities matching the generated query.
    /// The returned stream must always be used within a `try-with-resources` block
    /// to ensure that the underlying result set is properly closed.
    ///
    public final Stream<T> streamEntities(final int fetchSize, final Long... ids) {
        return streamEntities(generateQuery(), fetchSize, ids);
    }

    /// Returns a stream of entities matching the provided query.
    /// The returned stream must always be used within a `try-with-resources` block
    /// to ensure that the underlying result set is properly closed.
    ///
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

    /// Returns the first `numberOfEntities` entities that match the given `queryModel`.
    ///
    /// @param queryModel the query model used to fetch the entities
    ///
    public final List<T> getFirstEntities(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final int numberOfEntities) {
        if (getManagedType().equals(getEntityClass())) {
            return dao.getFirstEntities(queryModel, numberOfEntities);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.getFirstEntities(queryModel, numberOfEntities);
        }
    }

    /// Returns the first result page for the given query model, including summary information.
    ///
    /// @param queryModel the query model for which the first result page should be returned
    /// @param pageSize   the size of the page
    ///
    public final IPage<T> firstPage(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final QueryExecutionModel<T, EntityResultQueryModel<T>> totalsModel, final int pageSize) {
        if (getManagedType().equals(getEntityClass())) {
            return dao.firstPage(queryModel, pageSize);
        } else {
            generatedEntityController.setEntityType(getManagedType());
            return generatedEntityController.firstPage(queryModel, totalsModel, pageSize);
        }
    }

    /// Converts an existing property model (which has separate properties for `from`/`to`, `is`/`isNot`, and so on)
    /// into a new property model with a separate abstraction for each criterion.
    ///
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

    public IGeneratedEntityController<T> getGeneratedEntityController() {
        return generatedEntityController;
    }

    public ISerialiser getSerialiser() {
        return serialiser;
    }

    /// Returns the default `controllerProvider`.
    ///
    public ICompanionObjectFinder getControllerProvider() {
        return controllerProvider;
    }

    /// Initialises a crit-only single prototype entity if it has not been initialised before,
    /// and returns it as a result.
    ///
    /// @param entityType the type of the prototype (used only for initialisation)
    /// @param id         the ID of the prototype (used only for initialisation)
    ///
    public AbstractEntity<?> critOnlySinglePrototypeInit(final Class<AbstractEntity<?>> entityType, final Long id) {
        if (critOnlySinglePrototype == null) {
            critOnlySinglePrototype = getEntityFactory().newEntity(entityType, id);
            critOnlySinglePrototype.resetMetaState();
            // The initialisation phase starts here, so that definers are executed with the `isInitialising = true` flag.
            // Validation is deferred until the initialisation phase ends.
            critOnlySinglePrototype.beginInitialising();
        }
        return critOnlySinglePrototype();
    }

    /// Returns the crit-only single prototype entity.
    ///
    public AbstractEntity<?> critOnlySinglePrototype() {
        return critOnlySinglePrototype;
    }

    /// Returns the crit-only single prototype entity, if it exists.
    ///
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