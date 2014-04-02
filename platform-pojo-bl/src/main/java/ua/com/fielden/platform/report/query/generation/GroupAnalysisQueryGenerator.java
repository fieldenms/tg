package ua.com.fielden.platform.report.query.generation;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.swing.review.DynamicFetchBuilder;
import ua.com.fielden.platform.swing.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.swing.review.DynamicParamBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.utils.Pair;

public abstract class GroupAnalysisQueryGenerator<T extends AbstractEntity<?>> implements IReportQueryGenerator<T> {

    private final Class<T> root;
    private final ICentreDomainTreeManagerAndEnhancer cdtme;
    private final IAbstractAnalysisDomainTreeManager adtm;

    public GroupAnalysisQueryGenerator(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer cdtme, final IAbstractAnalysisDomainTreeManager adtm) {
        this.root = root;
        this.cdtme = cdtme;
        this.adtm = adtm;
    }

    protected IAbstractAnalysisDomainTreeManager adtm() {
        return adtm;
    }

    protected final Class<T> getRoot() {
        return root;
    }

    protected final ICentreDomainTreeManagerAndEnhancer getCdtme() {
        return cdtme;
    }

    @SuppressWarnings("unchecked")
    protected IQueryComposer<T> createQueryAndGroupBy(final Class<T> genClass, final List<String> distributionProperties) {
        final Class<T> managedType = (Class<T>) getCdtme().getEnhancer().getManagedType(getRoot());
        final List<String> aggregationProperties = adtm().getSecondTick().usedProperties(getRoot());

        final Map<String, String> yieldMap = createAnalysisPropertyMap(distributionProperties);
        yieldMap.putAll(createAnalysisPropertyMap(aggregationProperties));

        //Create base sub-query and query models.
        final EntityResultQueryModel<T> subQueryModel = DynamicQueryBuilder.createQuery(managedType, ReportQueryGenerationUtils.createQueryProperties(getRoot(), getCdtme())).model();
        //TODO the DynamicQueryBuilder.createAggregationQuery method must not receive genClass this is interim solution in order to distinguish money properties and add .mount prefix.
        final EntityResultQueryModel<T> queryModel = DynamicQueryBuilder.createAggregationQuery(subQueryModel, distributionProperties, genClass, yieldMap).modelAsEntity(genClass);

        final List<Pair<String, Ordering>> analysisOrderingProperties = new ArrayList<>();
        for (final Pair<String, Ordering> orderingProp : adtm().getSecondTick().orderedProperties(getRoot())) {
            analysisOrderingProperties.add(new Pair<>(yieldMap.get(orderingProp.getKey()), orderingProp.getValue()));
        }
        if (analysisOrderingProperties.isEmpty()) {
            for (final String groupOrder : distributionProperties) {
                analysisOrderingProperties.add(new Pair<>(yieldMap.get(groupOrder), Ordering.ASCENDING));
            }
        }

        //Creating the parameters map.
        final Map<String, Pair<Object, Object>> paramMap = EntityQueryCriteriaUtils.createParamValuesMap(getRoot(), managedType, getCdtme().getFirstTick());

        final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = from(queryModel).with(DynamicOrderingBuilder.createOrderingModel(genClass, analysisOrderingProperties))//
        .with(DynamicFetchBuilder.createFetchOnlyModel(genClass, new HashSet<String>(yieldMap.values())))//
        .with(DynamicParamBuilder.buildParametersMap(managedType, paramMap)).model();

        return createQueryComposer(resultQuery);
    }

    /**
     * Returns the map between real domain properties and generated with {@link AnalysisResultClass}.
     * 
     * @param properties
     * @return
     */
    private Map<String, String> createAnalysisPropertyMap(final List<String> properties) {
        final Map<String, String> propertiesMap = new LinkedHashMap<>();
        for (final String property : properties) {
            propertiesMap.put(property, getAnalysisPropertyName(property));
        }
        return propertiesMap;
    }

    /**
     * Returns the name of the property generated with {@link AnalysisResultClass} for the specified analysis property name.
     * 
     * @param property
     * @return
     */
    private String getAnalysisPropertyName(final String property) {
        return AnalysisResultClass.getAnalysisPropertyName(property);
    }

    private IQueryComposer<T> createQueryComposer(final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery) {
        return new IQueryComposer<T>() {

            @Override
            public ICompleted<T> getQuery() {
                throw new UnsupportedOperationException("Not yet implemented");
            }

            @Override
            public fetch<T> getFetch() {
                throw new UnsupportedOperationException("Not yet implemented");
            }

            @Override
            public OrderingModel getOrdering() {
                throw new UnsupportedOperationException("Not yet implemented");
            }

            @Override
            public Map<String, Object> getParams() {
                throw new UnsupportedOperationException("Not yet implemented");
            }

            @Override
            public QueryExecutionModel<T, EntityResultQueryModel<T>> composeQuery() {
                return resultQuery;
            }
        };
    }
}
