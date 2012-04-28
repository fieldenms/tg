package ua.com.fielden.platform.swing.review.development;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.criteria.enhanced.CriteriaProperty;
import ua.com.fielden.platform.criteria.enhanced.SecondParam;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyKeyException;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation.IAddToCriteriaTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.dynamictree.DynamicEntityTree;
import ua.com.fielden.platform.dynamictree.DynamicEntityTreeNode;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

@KeyType(String.class)
public abstract class EntityQueryCriteria<C extends ICentreDomainTreeManagerAndEnhancer, T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends AbstractEntity<String> {

    private static final long serialVersionUID = 9154466083364529734L;

    private final String ALIAS = "alias_for_main_criteria_type";

    @SuppressWarnings("rawtypes")
    private final Map<String, IValueMatcher> valueMatchers = new HashMap<String, IValueMatcher>();
    private final IValueMatcherFactory valueMatcherFactory;

    private final DAO dao;

    private final C cdtme;

    @Inject
    public EntityQueryCriteria(final IValueMatcherFactory valueMatcherFactory){
	this.valueMatcherFactory = valueMatcherFactory;

	//This values should be initialized through reflection.
	this.dao = null;
	this.cdtme = null;
    }

    public C getCentreDomainTreeMangerAndEnhancer(){
	return cdtme;
    }

    public Class<T> getEntityClass(){
	return dao.getEntityType();
    }

    /**
     * Must load default values for the properties of the binding entity.
     */
    public void defaultValues(){
	final IAddToCriteriaTickRepresentation ftr = getCentreDomainTreeMangerAndEnhancer().getRepresentation().getFirstTick();
	for(final Field propertyField : CriteriaReflector.getCriteriaProperties(getType())){
	    final SecondParam secondParam = propertyField.getAnnotation(SecondParam.class);
	    final CriteriaProperty critProperty = propertyField.getAnnotation(CriteriaProperty.class);
	    final Class<T> root = getEntityClass();
	    set(propertyField.getName(), secondParam == null ? ftr.getValueByDefault(root, critProperty.propertyName()) : ftr.getValue2ByDefault(root, critProperty.propertyName()));
	}
    }

    /**
     * Determines whether default values can be set or not.
     *
     * @return
     */
    public boolean isDefaultEnabled(){
	return !CriteriaReflector.getCriteriaProperties(getType()).isEmpty();
    }

    @SuppressWarnings("unchecked")
    public IValueMatcher<?> getValueMatcher(final String propertyName) {
	if (valueMatchers.get(propertyName) == null) {
	    valueMatchers.put(propertyName, valueMatcherFactory.getValueMatcher((Class<? extends AbstractEntity<?>>) getType(), propertyName));
	}
	return valueMatchers.get(propertyName);
    }

    /**
     * Run the configured entity query.
     * 
     * @param pageSize
     * @return
     */
    public final IPage<T> run(final int pageSize){
	final EntityResultQueryModel<T> notOrderedQuery = createQuery().model();
	return dao.firstPage(from(notOrderedQuery).with(createOrderingModel()).with(createFetchModel()).build(), pageSize);
    }

    /**
     * Returns the first result page for query model. The page size is specified with second parameter.
     * 
     * @param queryModel - query model for which the first result page must be returned.
     * @param pageSize - the page size.
     * @return
     */
    protected final IPage<T> firstPage(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final int pageSize){
	return dao.firstPage(queryModel, pageSize);
    }

    /**
     * Creates "fetch property" model for entity query criteria.
     * 
     * @return
     */
    protected fetch<T> createFetchModel() {
	try {
	    final DynamicEntityTree<T> fetchTree = new DynamicEntityTree<T>(//
		    getCentreDomainTreeMangerAndEnhancer().getSecondTick().checkedProperties(getEntityClass()), getEntityClass());
	    final fetch<T> main = buildFetchModels(getEntityClass(), fetchTree.getRoot());
	    return main;
	} catch (final Exception e1) {
	    throw new RuntimeException(e1);
	}
    }

    /**
     * Returns the ordering model for this query criteria.
     * 
     * @return
     */
    protected OrderingModel createOrderingModel(){
	IOrderingItemCloseable closeOrderable = null;
	for(final Pair<String, Ordering> orderPair : getCentreDomainTreeMangerAndEnhancer().getSecondTick().orderedProperties(getEntityClass())){
	    final IOrderingItem orderingItem = closeOrderable == null ? orderBy() : closeOrderable;
	    final ExpressionModel expression = getExpressionForProp(orderPair.getKey());
	    final ISingleOperandOrderable part = expression == null ? orderingItem.prop(createNotInitialisedQueryProperty(orderPair.getKey()).getConditionBuildingName()) : orderingItem.expr(expression);
	    closeOrderable = orderPair.getValue().equals(Ordering.ASCENDING) ? part.asc() : part.desc();
	}
	return closeOrderable == null ? null : closeOrderable.model();
    }

    /**
     * Returns the not configured query property instance for the specified property.
     * 
     * @param propertyName
     * @return
     */
    protected QueryProperty createNotInitialisedQueryProperty(final String propertyName){
	return new QueryProperty(getEntityClass(), propertyName, ALIAS);
    }

    /**
     * Creates the query with configured conditions.
     * 
     * @return
     */
    protected ICompleted createQuery(){
	return DynamicQueryBuilder.buildConditions(createJoinCondition(), createQueryProperties(), ALIAS);
    }

    /**
     * Returns the expression for calculated property specified with propName parameter. If the property is not calculated then returns null.
     * 
     * @param propName - the name of the calculated property.
     * @return
     */
    private ExpressionModel getExpressionForProp(final String propName) {
	try {
	    return ((CalculatedProperty) getCentreDomainTreeMangerAndEnhancer().getEnhancer().getCalculatedProperty(getEntityClass(), propName)).getAst().getModel();
	} catch (final IncorrectCalcPropertyKeyException e) {
	    return null;
	}
    }

    /**
     * Starts query building with appropriate join condition.
     * 
     * @return
     */
    private IJoin createJoinCondition() {
	try {
	    return select(getEntityClass()).as(ALIAS);
	} catch (final Exception e) {
	    throw new RuntimeException("Can not create join condition due to: " + e + ".");
	}
    }

    /**
     * Converts existing properties model (which has separate properties for from/to, is/isNot and so on)
     * into new properties model (which has single abstraction for one criterion).
     *
     * @param properties
     * @return
     */
    private List<QueryProperty> createQueryProperties() {
	final List<QueryProperty> queryProperties = new ArrayList<QueryProperty>();
	for (final String actualProperty : getCentreDomainTreeMangerAndEnhancer().getFirstTick().checkedProperties(getEntityClass())) {
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
	final IAddToCriteriaTickManager tickManager = getCentreDomainTreeMangerAndEnhancer().getFirstTick();
	final Class<T> root = getEntityClass();

	final QueryProperty queryProperty = createNotInitialisedQueryProperty(actualProperty);

	try{queryProperty.setValue(tickManager.getValue(root, actualProperty));}catch (final Exception e) {}
	try{queryProperty.setExclusive(tickManager.getExclusive(root, actualProperty));}catch (final Exception e) {}
	try{queryProperty.setValue2(tickManager.getValue2(root, actualProperty));}catch (final Exception e) {}
	try{queryProperty.setExclusive2(tickManager.getExclusive2(root, actualProperty));}catch (final Exception e) {}
	try{queryProperty.setDatePrefix(tickManager.getDatePrefix(root, actualProperty));}catch (final Exception e) {}
	try{queryProperty.setDateMnemonic(tickManager.getDateMnemonic(root, actualProperty));}catch (final Exception e) {}
	try{queryProperty.setAndBefore(tickManager.getAndBefore(root, actualProperty));}catch (final Exception e) {}
	try{queryProperty.setOrNull(tickManager.getOrNull(root, actualProperty));}catch (final Exception e) {}
	try{queryProperty.setNot(tickManager.getNot(root, actualProperty));}catch (final Exception e) {}
	return queryProperty;
    }

    /**
     * Builds the fetch model for subtree specified with treeNode parameter.
     * 
     * @param entityType - The type for fetch model.
     * @param treeNode - the root of subtree for which fetch model must be build.
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private fetch<T> buildFetchModels(final Class<T> entityType, final DynamicEntityTreeNode treeNode) throws Exception {
	fetch<T> fetchModel = fetch(entityType);

	if (treeNode == null || treeNode.getChildCount() == 0) {
	    return fetchModel;
	}

	for (final DynamicEntityTreeNode dynamicTreeNode : treeNode.getChildren()) {
	    final Class<?> propertyType = dynamicTreeNode.getType();
	    if (!EntityUtils.isEntityType(propertyType)) {
		continue;
	    }
	    final fetch<T> fetchSubModel = buildFetchModels((Class<T>) propertyType, dynamicTreeNode);
	    fetchModel = fetchModel.with(dynamicTreeNode.getName(), fetchSubModel);
	}

	return fetchModel;
    }
}