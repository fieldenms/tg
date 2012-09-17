package ua.com.fielden.platform.report.query.generation;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.GroupAnnotation;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.swing.review.DynamicFetchBuilder;
import ua.com.fielden.platform.swing.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.swing.review.DynamicParamBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.utils.Pair;

public class ChartAnalysisQueryGenerator<T extends AbstractEntity<?>> implements IReportQueryGeneration<T> {

    private final Class<T> root;
    private final ICentreDomainTreeManagerAndEnhancer cdtme;
    private final IAnalysisDomainTreeManager adtm;

    public ChartAnalysisQueryGenerator(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer cdtme, final IAnalysisDomainTreeManager adtm){
	this.root = root;
	this.cdtme = cdtme;
	this.adtm = adtm;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<QueryExecutionModel<T, EntityResultQueryModel<T>>> generateQueryModel() {
	final IDomainTreeEnhancer enhancer = cdtme.getEnhancer();
	final Class<T> managedType = /*createTypeWithGroupProps();*/(Class<T>)enhancer.getManagedType(root);
	final List<String> distributionProperties = adtm.getFirstTick().usedProperties(root);
	final List<String> aggregationProperties = adtm.getSecondTick().usedProperties(root);

	final EntityResultQueryModel<T> subQueryModel = DynamicQueryBuilder.createQuery(managedType, ReportQueryGenerationUtils.createQueryProperties(root, cdtme)).model();

	final List<Pair<String, ExpressionModel>> aggregation = getPropertyExpressionPair(aggregationProperties);

	final List<String> yieldProperties = new ArrayList<String>();
	yieldProperties.addAll(distributionProperties);
	yieldProperties.addAll(aggregationProperties);

	final EntityResultQueryModel<T> queryModel = DynamicQueryBuilder.createAggregationQuery(subQueryModel, distributionProperties, aggregation).modelAsEntity(managedType);

	final List<Pair<String, Ordering>> orderingProperties = new ArrayList<>(adtm.getSecondTick().orderedProperties(root));
	if(orderingProperties.isEmpty()){
	    for(final String groupOrder : distributionProperties){
		orderingProperties.add(new Pair<>(groupOrder, Ordering.ASCENDING));
	    }
	}

	//Creating the parameters map.
	final Map<String, Pair<Object, Object>> paramMap = EntityQueryCriteriaUtils.createParamValuesMap(root, managedType, cdtme.getFirstTick());

	final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = from(queryModel)
	.with(DynamicOrderingBuilder.createOrderingModel(managedType, orderingProperties))//
	.with(DynamicFetchBuilder.createFetchModel(managedType, new HashSet<String>(yieldProperties)))//
	.with(DynamicParamBuilder.buildParametersMap(managedType, paramMap)).model();

	final List<QueryExecutionModel<T, EntityResultQueryModel<T>>> result = new ArrayList<>();
	result.add(resultQuery);
	return result;
    }

    private Pair<Class<T>, List<String>> createTypeWithGroupProps() {
	final Class<T> managedType = (Class<T>)cdtme.getEnhancer().getManagedType(root);
	final List<String> distributionProperties = adtm.getFirstTick().usedProperties(root);
	final List<String> newGroupPropNames = new ArrayList<>();
	final List<NewProperty> groupProps = new ArrayList<>();
	final String predefinedRootTypeName = new DynamicTypeNamingService().nextTypeName(managedType.getName());
	int counter = 0;
	for(final String distrProp : distributionProperties){
	    if(distrProp.contains(".")){
		final NewProperty newProperty = createGroupProperty(predefinedRootTypeName, distrProp, counter++);
	    }
	}
	return null;
    }

    @SuppressWarnings("unchecked")
    private NewProperty createGroupProperty(final String definedClassName, final String propertyName, final int counter) {
	final Class<T> managedType = (Class<T>)cdtme.getEnhancer().getManagedType(root);
	final Class<?> type = StringUtils.isEmpty(propertyName) ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, propertyName);
	final Annotation calcAnnotation = new CalculatedAnnotation().contextualExpression(propertyName)//
		.rootTypeName(definedClassName)//
		.contextPath("")//
		.origination(null)//
		.attribute(CalculatedPropertyAttribute.NO_ATTR)//
		.category(CalculatedPropertyCategory.EXPRESSION)//
		.newInstance();
	final Annotation groupAnnotation = new GroupAnnotation(propertyName).newInstance();
	return new NewProperty("_group_analysis_property_#" + counter, type, false, "", "", calcAnnotation);
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
