package ua.com.fielden.platform.report.query.generation;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.swing.review.DynamicFetchBuilder;
import ua.com.fielden.platform.swing.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.utils.Pair;

public class PivotAnalysisQueryGgenerator<T extends AbstractEntity<?>> implements IReportQueryGeneration<T> {

    private final Class<T> root;
    private final ICentreDomainTreeManagerAndEnhancer cdtme;
    private final IPivotDomainTreeManager pdtm;

    public PivotAnalysisQueryGgenerator(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer cdtme, final IPivotDomainTreeManager pdtm){
	this.root = root;
	this.cdtme = cdtme;
	this.pdtm = pdtm;
    }

    @Override
    public List<QueryExecutionModel<T, EntityResultQueryModel<T>>> generateQueryModel() {
	final List<String> distributionProperties = pdtm.getFirstTick().usedProperties(root);

	final List<String> groups = new ArrayList<String>();
	final List<QueryExecutionModel<T, EntityResultQueryModel<T>>> resultQueryList = new ArrayList<>();
	resultQueryList.add(generateQueryModel(groups));
	for(final String groupProperty : distributionProperties){
	    groups.add(groupProperty);
	    resultQueryList.add(generateQueryModel(groups));
	}
	return resultQueryList;
    }

    @SuppressWarnings("unchecked")
    private QueryExecutionModel<T, EntityResultQueryModel<T>> generateQueryModel(final List<String> distributionProperties) {
	final IDomainTreeEnhancer enhancer = cdtme.getEnhancer();
	final Class<T> managedType = (Class<T>)enhancer.getManagedType(root);
	final List<String> aggregationProperties = pdtm.getSecondTick().usedProperties(root);

	final EntityResultQueryModel<T> subQueryModel = DynamicQueryBuilder.createQuery(managedType, ReportQueryGenerationUtils.createQueryProperties(root, cdtme)).model();

	final List<Pair<String, ExpressionModel>> aggregation = getPropertyExpressionPair(aggregationProperties);

	final List<String> yieldProperties = new ArrayList<String>();
	yieldProperties.addAll(distributionProperties);
	yieldProperties.addAll(aggregationProperties);

	final EntityResultQueryModel<T> queryModel = DynamicQueryBuilder.createAggregationQuery(subQueryModel, ReportQueryGenerationUtils.createQueryProperties(root, cdtme), distributionProperties, aggregation).modelAsEntity(managedType);

	final List<Pair<String, Ordering>> orderingProperties = new ArrayList<>(pdtm.getSecondTick().orderedProperties(root));
	if(orderingProperties.isEmpty()){
	    for(final String groupOrder : distributionProperties){
		orderingProperties.add(new Pair<>(groupOrder, Ordering.ASCENDING));
	    }
	}

	final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = from(queryModel)
	.with(DynamicOrderingBuilder.createOrderingModel(managedType, orderingProperties))//
	.with(DynamicFetchBuilder.createFetchModel(managedType, new HashSet<String>(yieldProperties))).model();

	return resultQuery;
    }



    /**
     * Returns the list of property name and it's expression model pairs.
     *
     * @param propertyForExpression
     * @return
     */
    private List<Pair<String, ExpressionModel>> getPropertyExpressionPair(final List<String> propertyForExpression){
        final IDomainTreeEnhancer enhancer = cdtme.getEnhancer();
        final List<Pair<String, ExpressionModel>> propertyExpressionPair = new ArrayList<>();
        for (final String property : propertyForExpression) {
            final ExpressionModel expression = EntityQueryCriteriaUtils.getExpressionForProp(root, property, enhancer);
            propertyExpressionPair.add(new Pair<>(property, expression));
        }
        return propertyExpressionPair;
    }

}
