package ua.com.fielden.platform.report.query.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.swing.review.DynamicFetchBuilder;
import ua.com.fielden.platform.swing.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.swing.review.DynamicParamBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Query model generator for grid analysis.
 * 
 * @author TG Team
 * 
 * @param <T>
 * @param <CDTME>
 */
public class GridAnalysisQueryGenerator<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> implements IReportQueryGenerator<T> {

    private final Class<T> root;
    private final CDTME cdtme;

    public GridAnalysisQueryGenerator(final Class<T> root, final CDTME cdtme) {
        this.root = root;
        this.cdtme = cdtme;
    }

    @Override
    public AnalysisResultClassBundle<T> generateQueryModel() {
        final ICompleted<T> query = createQuery();
        final fetch<T> fetchModel = createFetchModel();
        final fetch<T> totalFetchModel = createTotalFetchModel();
        final OrderingModel ordering = createOrderingModel();
        final Map<String, Object> propValues = createParamValues();
        final List<IQueryComposer<T>> queryModels = new ArrayList<>();
        queryModels.add(createQueryComposer(query, fetchModel, ordering, propValues));
        if (totalFetchModel != null) {
            queryModels.add(createQueryComposer(query, totalFetchModel, null, propValues));
        }
        return new AnalysisResultClassBundle<>(cdtme, null, null, queryModels);
    }

    /**
     * Returns the original entity type.
     * 
     * @return
     */
    public Class<T> entityClass() {
        return root;
    }

    /**
     * Returns the enhanced entity type. If the entity type wasn't enhanced then the original will be returned.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public Class<T> enhancedType() {
        return (Class<T>) cdtme.getEnhancer().getManagedType(root);
    }

    /**
     * Returns the {@link EntityResultQueryModel} instance, that is used for query generation in the {@link #createQueryExecutionModel()} routine. Override this to provide custom
     * query generation.
     * 
     * @return
     */
    public ICompleted<T> createQuery() {
        return DynamicQueryBuilder.createQuery(enhancedType(), ReportQueryGenerationUtils.createQueryProperties(root, cdtme));
    }

    /**
     * Returns the {@link fetch} instance that is used for query generation.
     * 
     * @return
     */
    public fetch<T> createFetchModel() {
        final IAddToResultTickManager resultTickManager = cdtme.getSecondTick();
        final IDomainTreeEnhancer enhancer = cdtme.getEnhancer();
        final Pair<Set<String>, Set<String>> separatedFetch = EntityQueryCriteriaUtils.separateFetchAndTotalProperties(entityClass(), resultTickManager, enhancer);
        return DynamicFetchBuilder.createFetchOnlyModel(enhancedType(), separatedFetch.getKey());
    }

    /**
     * Returns the {@link fetch} model instance for summary query. If there are no total properties then null will be returned.
     * 
     * @return
     */
    public fetch<T> createTotalFetchModel() {
        final IAddToResultTickManager resultTickManager = cdtme.getSecondTick();
        final IDomainTreeEnhancer enhancer = cdtme.getEnhancer();
        final Pair<Set<String>, Set<String>> separatedFetch = EntityQueryCriteriaUtils.separateFetchAndTotalProperties(entityClass(), resultTickManager, enhancer);
        return separatedFetch.getValue().isEmpty() ? null : DynamicFetchBuilder.createTotalFetchModel(enhancedType(), separatedFetch.getValue());
    }

    /**
     * Creates the {@link OrderingModel} instance that is used for query generation.
     * 
     * @return
     */
    public OrderingModel createOrderingModel() {
        final IAddToResultTickManager resultTickManager = cdtme.getSecondTick();
        return DynamicOrderingBuilder.createOrderingModel(enhancedType(), resultTickManager.orderedProperties(root));
    }

    /**
     * Returns the map between parameter name and it's value needed for query generation.
     * 
     * @return
     */
    public Map<String, Object> createParamValues() {
        final IAddToCriteriaTickManager criteriaTickManager = cdtme.getFirstTick();
        final Map<String, Pair<Object, Object>> paramMap = EntityQueryCriteriaUtils.createParamValuesMap(entityClass(), enhancedType(), criteriaTickManager);
        return DynamicParamBuilder.buildParametersMap(enhancedType(), paramMap);
    }

    /**
     * Concatenates appropriate functions to specified query and returns the result. It can be used to continue chaining conditions to the given query.
     * 
     * @param query
     * @return
     */
    public static <T extends AbstractEntity<?>> IWhere0<T> where(final ICompleted<T> query) {
        if (query instanceof IJoin) {
            return ((IJoin<T>) query).where();
        } else {
            return ((ICompoundCondition0<T>) query).and();
        }
    }

    /**
     * Returns the property that can be used for building query condition.
     * 
     * @param propertyName
     * @return
     */
    public static String property(final String propertyName) {
        return DynamicQueryBuilder.createConditionProperty(propertyName);
    }

    public CDTME getCdtme() {
        return cdtme;
    }

    /**
     * Creates query composer for the specified query ,fetch model, ordering model and map between parameter names and their values.
     * 
     * @param query
     * @param fetchModel
     * @param ordering
     * @param paramValues
     * @return
     */
    private IQueryComposer<T> createQueryComposer(//
    final ICompleted<T> query, //
            final fetch<T> fetchModel, //
            final OrderingModel ordering, //
            final Map<String, Object> paramValues) {
        return new AbstractQueryComposer<T>() {

            @Override
            public ICompleted<T> getQuery() {
                return query;
            }

            @Override
            public fetch<T> getFetch() {
                return fetchModel;
            }

            @Override
            public OrderingModel getOrdering() {
                return ordering;
            }

            @Override
            public Map<String, Object> getParams() {
                return paramValues;
            }
        };
    }
}
