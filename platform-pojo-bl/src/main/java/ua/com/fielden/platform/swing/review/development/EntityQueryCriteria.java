package ua.com.fielden.platform.swing.review.development;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.criteria.enhanced.CriteriaProperty;
import ua.com.fielden.platform.criteria.enhanced.SecondParam;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyKeyException;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation.IAddToCriteriaTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer.ByteArray;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.review.DynamicFetchBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

@KeyType(String.class)
public abstract class EntityQueryCriteria<C extends ICentreDomainTreeManagerAndEnhancer, T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends AbstractEntity<String> {

    private static final long serialVersionUID = 9154466083364529734L;

    @SuppressWarnings("rawtypes")
    private final Map<String, IValueMatcher> valueMatchers = new HashMap<String, IValueMatcher>();
    private final IValueMatcherFactory valueMatcherFactory;

    private final DAO dao;
    private final IGeneratedEntityController<T> generatedEntityController;

    private final C cdtme;

    @Inject
    public EntityQueryCriteria(final IValueMatcherFactory valueMatcherFactory){
	this.valueMatcherFactory = valueMatcherFactory;

	//This values should be initialized through reflection.
	this.dao = null;
	this.generatedEntityController = null;
	this.cdtme = null;
    }

    public C getCentreDomainTreeMangerAndEnhancer(){
	return cdtme;
    }

    /**
     * Returns the root for which this criteria was generated.
     *
     * @return
     */
    public Class<T> getEntityClass(){
	return dao.getEntityType();
    }

    /**
     * Returns the enhanced type for entity class. The entity class is retrieved with {@link #getEntityClass()} method.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public Class<T> getManagedType(){
	return (Class<T>)getCentreDomainTreeMangerAndEnhancer().getEnhancer().getManagedType(getEntityClass());
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
    public final IPage<T> run(final int pageSize) {
	final EntityResultQueryModel<T> notOrderedQuery = DynamicQueryBuilder.createQuery(getManagedType(), createQueryProperties()).model();
	final Pair<List<String>, List<String>> separatedFetch = separateTotalProperties();
	final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = from(notOrderedQuery)//
		.with(createOrderingModel())//
		.with(DynamicFetchBuilder.createFetchModel(getManagedType(), separatedFetch.getKey())).build();
	if (!separatedFetch.getValue().isEmpty()) {
	    final QueryExecutionModel<T, EntityResultQueryModel<T>> totalQuery = from(notOrderedQuery)//
		    .with(DynamicFetchBuilder.createTotalFetchModel(getManagedType(), separatedFetch.getValue())).build();
	    return firstPage(resultQuery, totalQuery, pageSize);
	} else {
	    return firstPage(resultQuery, pageSize);
	}
    }

    /**
     * Removes the passed entity. If the entity is not of the entity type or managed type of this criteria.
     *
     * @param entity
     */
    public void delete(final T entity){
	if(entity.getType().equals(getEntityClass()) || entity.getType().equals(getManagedType())){
	    final EntityResultQueryModel<T> deleteQuery = select(getEntityClass()).where().prop("id").eq().val(entity.getId()).model();
	    dao.delete(deleteQuery);
	}else{
	    throw new IllegalArgumentException("It's impossible to delete entity that isn't of the entity type or managed type of this criteria!");
	}
    }

//    /**
//     * Returns the fetch properties for this query criteria.
//     *
//     * @return
//     */
//    final List<String> getFetchProperties(){
//	return separateTotalProperties().getKey();
//    }
//
//    /**
//     * Returns the total properties for this query criteria.
//     *
//     * @return
//     */
//    public final List<String> getTotalProperties(){
//	return separateTotalProperties().getValue();
//    }

    /**
     * Converts existing properties model (which has separate properties for from/to, is/isNot and so on)
     * into new properties model (which has single abstraction for one criterion).
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
     * @param queryModel - query model for which the first result page must be returned.
     * @param pageSize - the page size.
     * @return
     */
    protected final IPage<T> firstPage(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final int pageSize){
	if(getManagedType().equals(getEntityClass())){
	    return dao.firstPage(queryModel, pageSize);
	}else{
	    generatedEntityController.setEntityType(getManagedType());
	    return generatedEntityController.firstPage(queryModel, pageSize, getByteArrayForManagedType());
	}
    }

    /**
     * Returns the first result page for query model with summary. The page size is specified with the third parameter.
     *
     * @param queryModel - query model for which the first result page must be returned.
     * @param pageSize - the page size.
     * @return
     */
    protected final IPage<T> firstPage(final QueryExecutionModel<T, EntityResultQueryModel<T>> queryModel, final QueryExecutionModel<T, EntityResultQueryModel<T>> totalsModel, final int pageSize){
	if(getManagedType().equals(getEntityClass())){
	    return dao.firstPage(queryModel, pageSize);
	}else{
	    generatedEntityController.setEntityType(getManagedType());
	    return generatedEntityController.firstPage(queryModel, totalsModel, pageSize, getByteArrayForManagedType());
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
	return new QueryProperty(getManagedType(), propertyName);
    }

    /**
     * Separates total properties from fetch properties.
     *
     * @return
     */
    private Pair<List<String>, List<String>> separateTotalProperties() {
        final List<String> fetchProperties = new ArrayList<String>();
        final List<String> totalProperties = new ArrayList<String>();
        final List<String> checkedProperties = cdtme.getSecondTick().checkedProperties(getEntityClass());
        for (final String property : checkedProperties)
            try {
        	final ICalculatedProperty calcProperty = cdtme.getEnhancer().getCalculatedProperty(getEntityClass(), property);
        	final String originProperty = Reflector.fromRelative2AbsotulePath(calcProperty.getContextPath(), calcProperty.getOriginationProperty());
		if(calcProperty.category() == CalculatedPropertyCategory.AGGREGATED_EXPRESSION){
		    if(checkedProperties.contains(originProperty)){
			totalProperties.add(property);
		    }
		} else {
        	    fetchProperties.add(property);
        	}
            } catch (final IncorrectCalcPropertyKeyException ex) {
        	fetchProperties.add(property);
            }
        return new Pair<List<String>, List<String>>(fetchProperties, totalProperties);
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

	queryProperty.setValue(tickManager.getValue(root, actualProperty));
	if (AbstractDomainTree.isDoubleCriterionOrBoolean(getManagedType(), actualProperty)) {
	    queryProperty.setValue2(tickManager.getValue2(root, actualProperty));
	}
	if (AbstractDomainTree.isDoubleCriterion(getManagedType(), actualProperty)) {
	    queryProperty.setExclusive(tickManager.getExclusive(root, actualProperty));
	    queryProperty.setExclusive2(tickManager.getExclusive2(root, actualProperty));
	}
	final Class<?> propertyType = StringUtils.isEmpty(actualProperty) ? getManagedType() : PropertyTypeDeterminator.determinePropertyType(root, actualProperty);
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
    private List<byte[]> getByteArrayForManagedType(){
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
}