package ua.com.fielden.platform.report.query.generation;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.review.DynamicFetchBuilder;
import ua.com.fielden.platform.swing.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.swing.review.DynamicParamBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Query model generator for grid analysis.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <CDTME>
 */
public class GridAnalysisQueryGenerator<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> implements IReportQueryGeneration<T> {

    private final Class<T> root;
    private final CDTME cdtme;

    public GridAnalysisQueryGenerator(final Class<T> root, final CDTME cdtme){
	this.root = root;
	this.cdtme = cdtme;
    }

    @Override
    public AnalysisResultClassBundle<T> generateQueryModel() {
	final IAddToResultTickManager resultTickManager = cdtme.getSecondTick();
	final IAddToCriteriaTickManager criteriaTickManager = cdtme.getFirstTick();
	final IDomainTreeEnhancer enhancer = cdtme.getEnhancer();
	final Pair<Set<String>, Set<String>> separatedFetch = EntityQueryCriteriaUtils.separateFetchAndTotalProperties(entityClass(), resultTickManager, enhancer);
	final Map<String, Pair<Object, Object>> paramMap = EntityQueryCriteriaUtils.createParamValuesMap(entityClass(), enhancedType(), criteriaTickManager);
	final EntityResultQueryModel<T> notOrderedQuery = createQueryModel();
	final List<QueryExecutionModel<T, EntityResultQueryModel<T>>> queryModels = new ArrayList<>();
	final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = from(notOrderedQuery)//
		.with(DynamicOrderingBuilder.createOrderingModel(enhancedType(), resultTickManager.orderedProperties(root)))//
		.with(DynamicFetchBuilder.createFetchOnlyModel(enhancedType(), separatedFetch.getKey()))//
		.with(DynamicParamBuilder.buildParametersMap(enhancedType(), paramMap)).model();
	queryModels.add(resultQuery);
	if (!separatedFetch.getValue().isEmpty()) {
	    final QueryExecutionModel<T, EntityResultQueryModel<T>> totalQuery = from(notOrderedQuery)//
		    .with(DynamicFetchBuilder.createTotalFetchModel(enhancedType(), separatedFetch.getValue()))//
		    .with(DynamicParamBuilder.buildParametersMap(enhancedType(), paramMap)).model();
	    queryModels.add(totalQuery);
	}
	return new AnalysisResultClassBundle<>(null, null, queryModels);
    }

    /**
     * Returns the original entity type.
     *
     * @return
     */
    public Class<T> entityClass(){
	return root;
    }

    /**
     * Returns the enhanced entity type. If the entity type wasn't enhanced then the original will be returned.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public Class<T> enhancedType(){
	return (Class<T>)cdtme.getEnhancer().getManagedType(root);
    }

    /**
     * Returns the {@link EntityResultQueryModel} instance, that is used for query generation in the {@link #createQueryExecutionModel()} routine.
     * Override this to provide custom query generation.
     *
     * @return
     */
    public EntityResultQueryModel<T> createQueryModel(){
	return createBaseQueryModel().model();
    }

    /**
     * Creates query model based on {@link ICentreDomainTreeManagerAndEnhancer} instance only.
     *
     * @return
     */
    public final ICompleted<T> createBaseQueryModel(){
	return DynamicQueryBuilder.createQuery(enhancedType(), createQueryProperties());
    }

    /**
     * Concatenates appropriate functions to specified query and returns the result. It can be used to continue chaining conditions to the given query.
     *
     * @param query
     * @return
     */
    public final IWhere0<T> where(final ICompleted<T> query) {
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
    public final String property(final String propertyName){
	return DynamicQueryBuilder.createConditionProperty(propertyName);
    }

    /**
     * Converts existing properties model (which has separate properties for from/to, is/isNot and so on)
     * into new properties model (which has single abstraction for one criterion).
     *
     * @param properties
     * @return
     */
    private final List<QueryProperty> createQueryProperties() {
        final List<QueryProperty> queryProperties = new ArrayList<QueryProperty>();
        for (final String actualProperty : cdtme.getFirstTick().checkedProperties(entityClass())) {
            if (!AbstractDomainTree.isPlaceholder(actualProperty)) {
        	queryProperties.add(createQueryProperty(actualProperty));
            }
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
	final IAddToCriteriaTickManager tickManager = cdtme.getFirstTick();
	final QueryProperty queryProperty = EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(enhancedType(), actualProperty);

	queryProperty.setValue(tickManager.getValue(entityClass(), actualProperty));
	if (AbstractDomainTree.isDoubleCriterionOrBoolean(enhancedType(), actualProperty)) {
	    queryProperty.setValue2(tickManager.getValue2(entityClass(), actualProperty));
	}
	if (AbstractDomainTree.isDoubleCriterion(enhancedType(), actualProperty)) {
	    queryProperty.setExclusive(tickManager.getExclusive(entityClass(), actualProperty));
	    queryProperty.setExclusive2(tickManager.getExclusive2(entityClass(), actualProperty));
	}
	final Class<?> propertyType = StringUtils.isEmpty(actualProperty) ? enhancedType() : PropertyTypeDeterminator.determinePropertyType(enhancedType(), actualProperty);
	if (EntityUtils.isDate(propertyType)) {
	    queryProperty.setDatePrefix(tickManager.getDatePrefix(entityClass(), actualProperty));
	    queryProperty.setDateMnemonic(tickManager.getDateMnemonic(entityClass(), actualProperty));
	    queryProperty.setAndBefore(tickManager.getAndBefore(entityClass(), actualProperty));
	}
	queryProperty.setOrNull(tickManager.getOrNull(entityClass(), actualProperty));
	queryProperty.setNot(tickManager.getNot(entityClass(), actualProperty));
	return queryProperty;
    }
}
