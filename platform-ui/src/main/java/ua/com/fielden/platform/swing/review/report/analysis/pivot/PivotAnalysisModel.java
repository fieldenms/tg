package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingManager.IPropertyOrderingListener;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotAddToDistributionTickManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.DynamicFetchBuilder;
import ua.com.fielden.platform.swing.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.Pair;

public class PivotAnalysisModel<T extends AbstractEntity<?>> extends AbstractAnalysisReviewModel<T, ICentreDomainTreeManagerAndEnhancer, IPivotDomainTreeManager, Void> {

    private final PivotTreeTableModelEx pivotModel;

    public PivotAnalysisModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final IPivotDomainTreeManager adtme, final PageHolder pageHolder) {
	super(criteria, adtme, pageHolder);
	pivotModel = new PivotTreeTableModelEx();
    }

    public PivotTreeTableModel getPivotModel() {
        return pivotModel;
    }

    @Override
    protected Void executeAnalysisQuery() {

	final Class<T> root = getCriteria().getEntityClass();
	final List<String> distributionProperties = adtme().getFirstTick().usedProperties(root);

	final List<String> groups = new ArrayList<String>();
	final Map<String, List<T>> resultMap = new HashMap<String, List<T>>();
	resultMap.put("Grand total", getGroupList(groups));
	for(final String groupProperty : distributionProperties){
	    groups.add(groupProperty);
	    resultMap.put(groupProperty, getGroupList(groups));
	}
	pivotModel.loadData(resultMap, distributionProperties, adtme().getSecondTick().usedProperties(root));
	return null;
    }

    @Override
    protected Result canLoadData() {
	final Result result = getCriteria().isValid();
	if(!result.isSuccessful()){
	    return result;
	}
	final Class<T> entityClass = getCriteria().getEntityClass();
	if(adtme().getFirstTick().usedProperties(entityClass).isEmpty() && adtme().getSecondTick().usedProperties(entityClass).isEmpty()){
	    return new Result(new IllegalStateException("Please choose distribution or aggregation properties"));
	}
	return Result.successful(this);
    }

    @Override
    protected void exportData(final String fileName) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    protected String[] getExportFileExtensions() {
        return new String[] {getDefaultExportFileExtension()};
    }

    @Override
    protected String getDefaultExportFileExtension() {
        return "xls";
    }

    /**
     * Returns the page for the pivot analysis query grouped by specified list of properties.
     *
     * @param groups
     * @return
     */
    private List<T> getGroupList(final List<String> groups){
	final Class<T> root = getCriteria().getEntityClass();
	final List<String> aggregationProperties = adtme().getSecondTick().usedProperties(root);

	ICompleted<T> baseQuery = DynamicQueryBuilder.createQuery(getCriteria().getManagedType(), getCriteria().createQueryProperties());
	for (final String groupProperty : groups) {
	    baseQuery = getCriteria().groupBy(groupProperty, baseQuery);
	}
	final List<String> yieldProperties = new ArrayList<String>();
	yieldProperties.addAll(groups);
	yieldProperties.addAll(aggregationProperties);
	ISubsequentCompletedAndYielded<T> yieldedQuery = null;
	for (final String yieldProperty : yieldProperties){
	    yieldedQuery = yieldedQuery == null //
			? getCriteria().yield(yieldProperty, baseQuery) //
			: getCriteria().yield(yieldProperty, yieldedQuery);
	}
	if(yieldedQuery == null){
	    throw new IllegalStateException("The query was compound incorrectly!");
	}
	final EntityResultQueryModel<T> queryModel = yieldedQuery.modelAsEntity(getCriteria().getManagedType());

	final List<Pair<String, Ordering>> orderingProperties = new ArrayList<Pair<String,Ordering>>(adtme().getSecondTick().orderedProperties(root));
	if(orderingProperties.isEmpty()){
	    for(final String groupOrder : groups){
		orderingProperties.add(new Pair<String, Ordering>(groupOrder, Ordering.ASCENDING));
	    }
	}
	final List<Pair<Object, Ordering>> orderingPairs = EntityQueryCriteriaUtils.getOrderingList(root, //
		orderingProperties, //
		getCriteria().getCentreDomainTreeMangerAndEnhancer().getEnhancer());
	final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = from(queryModel)
	.with(DynamicOrderingBuilder.createOrderingModel(getCriteria().getManagedType(), orderingPairs))//
	.with(DynamicFetchBuilder.createFetchModel(getCriteria().getManagedType(), new HashSet<String>(yieldProperties))).model();

	return getCriteria().run(resultQuery);
    }

    private class PivotTreeTableModelEx extends PivotTreeTableModel {

	private static final char RIGHT_ARROW = '\u2192';

	private final List<String> distributionProperties = new ArrayList<String>();
	private final List<String> aggregationProperties = new ArrayList<String>();

	private final Comparator<MutableTreeTableNode> sorter = new AggregationSorter();

	public PivotTreeTableModelEx() {
	    adtme().getSecondTick().addPropertyOrderingListener(new IPropertyOrderingListener() {

		    @SuppressWarnings("unchecked")
		    @Override
		    public void propertyStateChanged(final Class<?> root, final String property, final List<Pair<String, Ordering>> newOrderedProperties, final List<Pair<String, Ordering>> oldState) {
			if (pivotModel.getRoot() != null) {
			    ((PivotTreeTableNodeEx) pivotModel.getRoot()).sort();
			    fireSorterChageEvent(new PivotSorterChangeEvent(PivotTreeTableModelEx.this));
			}
		    }
		});
	}

	@Override
	public int getColumnCount() {
	    final Class<T> root = getCriteria().getEntityClass();
	    final List<String> usedProperties = adtme().getSecondTick().usedProperties(root);
	    if(!usedProperties.isEmpty()){
		return 1 + usedProperties.size();
	    }
	    return adtme().getFirstTick().usedProperties(root).isEmpty() ? 0 : 1;
	}

	@Override
	public Class<?> getColumnClass(final int column) {
	    if (column == 0) {
		return String.class;
	    }
	    final Class<T> root = getCriteria().getEntityClass();
	    final Class<?> managedType = getCriteria().getManagedType();
	    final IPivotAddToAggregationTickManager secondTick = adtme().getSecondTick();
	    final String property = secondTick.usedProperties(root).get(column - 1);
	    return PropertyTypeDeterminator.determineClass(managedType, property, true, true);
	}

	@Override
	public String getColumnName(final int column) {
	    if (column == 0) {
		String name = "";
		for (final String columnEntry : distributionProperties) {
		    name += RIGHT_ARROW + columnEntry;
		}
		return name.isEmpty() ? "<html><i>(Distribution properties)</i></html>" : name.substring(1);
	    }
	    final Class<T> root = getCriteria().getEntityClass();
	    final IPivotAddToAggregationTickManager secondTick = adtme().getSecondTick();
	    final String property = secondTick.usedProperties(root).get(column - 1);
	    return property;
	}

	@Override
	int getColumnWidth(final int column) {
	    final Class<T> root = getCriteria().getEntityClass();
	    final IPivotAddToDistributionTickManager firstTick = adtme().getFirstTick();
	    final IPivotAddToAggregationTickManager secondTick = adtme().getSecondTick();
	    final List<String> distributionUsedProperties = firstTick.usedProperties(root);
	    final List<String> aggregationUsedProperties = secondTick.usedProperties(root);
	    if(column == 0 && !distributionUsedProperties.isEmpty()){
		return firstTick.getWidth(root, distributionUsedProperties.get(0));
	    } else if(column > 0) {
		return secondTick.getWidth(root, aggregationUsedProperties.get(column-1));
	    }
	    return 0;
	}

	@Override
	String getColumnTooltipAt(final int column) {
	    return getColumnName(column);
	}

	@Override
	List<String> categoryProperties() {
	    return Collections.unmodifiableList(distributionProperties);
	}

	@Override
	List<String> aggregatedProperties() {
	    return Collections.unmodifiableList(aggregationProperties);
	}

	@SuppressWarnings("unchecked")
	private final void loadData(final Map<String, List<T>> loadedData, final List<String> distributionProperties, final List<String> aggregationProperties){

	    //Loading the alias maps for the aggregation and category properties.
	    if(distributionProperties == null || aggregationProperties == null){
		return;
	    }
	    this.distributionProperties.clear();
	    this.distributionProperties.addAll(distributionProperties);
	    this.aggregationProperties.clear();
	    this.aggregationProperties.addAll(aggregationProperties);

	    final PivotTreeTableNodeEx root = new PivotTreeTableNodeEx("root", null);
	    final PivotTreeTableNodeEx grand = new PivotTreeTableNodeEx("Grand total", loadedData.get("Grand total").get(0));

	    final Map<Object, Pair<PivotTreeTableNodeEx,Map<?, ?>>> nodes = new HashMap<Object, Pair<PivotTreeTableNodeEx,Map<?, ?>>>();
	    for(int categoryIndex = 0; categoryIndex < distributionProperties.size(); categoryIndex++){
		final List<T> listToLoad = loadedData.get(distributionProperties.get(categoryIndex));
		for(final T data : listToLoad){
		    Map<Object, Pair<PivotTreeTableNodeEx,Map<?, ?>>> currentLevel = nodes;
		    PivotTreeTableNodeEx currentNode = grand;
		    for(int dataIndex = 0; dataIndex < categoryIndex; dataIndex++){
			final Object key = data.get(distributionProperties.get(dataIndex));
			final Pair<PivotTreeTableNodeEx,Map<?, ?>> nodePair = currentLevel.get(key);
			if(nodePair == null){
			    throw new IllegalStateException("The node with " + key + " wasn't found!");
			}
			currentLevel = (Map<Object, Pair<PivotTreeTableNodeEx,Map<?, ?>>>)nodePair.getValue();
			currentNode = nodePair.getKey();
		    }
		    final Object currentKey = data.get(distributionProperties.get(categoryIndex));
		    final PivotTreeTableNodeEx child = new PivotTreeTableNodeEx(currentKey, data);
		    final Pair<PivotTreeTableNodeEx, Map<?, ?>> childPair = new Pair<PivotTreeTableNodeEx, Map<?, ?>>(child, new HashMap<Object, Pair<PivotTreeTableNodeEx, Map<?, ?>>>());
		    currentLevel.put(currentKey, childPair);
		    currentNode.add(child);
		}
	    }
	    root.add(grand);
	    SwingUtilitiesEx.invokeLater(new Runnable() {

		@Override
		public void run() {
		    setRoot(root);
		}
	    });
	}

	private class PivotTreeTableNodeEx extends PivotTreeTableNode {

	    //private final static String NULL_USER_OBJECT = "UNKNOWN";

	    private final T aggregatedData;

	    public PivotTreeTableNodeEx(final Object userObject, final T aggregatedData){
		super(userObject);
		this.aggregatedData = aggregatedData;
	    }

	    @Override
	    public int getColumnCount() {
		final Class<T> root = getCriteria().getEntityClass();
		final List<String> usedProperties = adtme().getSecondTick().usedProperties(root);
		if(!usedProperties.isEmpty()){
		    return 1 + usedProperties.size();
		}
		return adtme().getFirstTick().usedProperties(root).isEmpty() ? 0 : 1;
	    }

	    @Override
	    public Object getValueAt(final int column) {
		if (column == 0) {
		    if (getUserObject() instanceof AbstractEntity) {
			final AbstractEntity<?> entity = (AbstractEntity<?>) getUserObject();
			return entity.getKey().toString() + (StringUtils.isEmpty(entity.getDesc()) ? "" : " - " + entity.getDesc());
		    }
		    return getUserObject();
		}
		final Class<T> root = getCriteria().getEntityClass();
		final IPivotAddToAggregationTickManager secondTick = adtme().getSecondTick();
		final String property = secondTick.usedProperties(root).get(column - 1);
		return aggregatedData.get(property);
	    }



	    /**
	     * Sort children of this node, using comparator defined in the model.
	     *
	     * @param treeTableSorter
	     */
	    @SuppressWarnings("unchecked")
	    private void sort() {
		for (final MutableTreeTableNode child : children) {
		    ((PivotTreeTableNodeEx) child).sort();
		}
		Collections.sort(children, sorter);
	    }

	    @SuppressWarnings("rawtypes")
	    @Override
	    public String getTooltipAt(final int column) {
		if (column == 0) {
		    if (getUserObject() instanceof AbstractEntity) {
			return ((AbstractEntity) getUserObject()).getDesc();
		    }
		    return getUserObject().toString();
		}
		final Object value = getValueAt(column);
		return value != null ? value.toString() : null;
	    }
	}

	private class AggregationSorter implements Comparator<MutableTreeTableNode> {

	    @SuppressWarnings("rawtypes")
	    @Override
	    public int compare(final MutableTreeTableNode o1, final MutableTreeTableNode o2) {

		final Class<T> root = getCriteria().getEntityClass();
		final IPivotAddToAggregationTickManager secondTick = adtme().getSecondTick();

		final List<Pair<String, Ordering>> sortObjects = secondTick.orderedProperties(root);
		if (sortObjects == null || sortObjects.isEmpty()) {
		    return defaultCompare(o1, o2);
		}
		final List<Pair<Integer, Ordering>> sortOrders = new ArrayList<Pair<Integer, Ordering>>();
		final List<String> columns = secondTick.usedProperties(root);
		for (final Pair<String, Ordering> aggreagationProperty : sortObjects) {
		    final int sortOrder = getIndex(aggreagationProperty.getKey(), columns);
		    if (sortOrder >= 0) {
			sortOrders.add(new Pair<Integer, Ordering>(Integer.valueOf(sortOrder), aggreagationProperty.getValue()));
		    }
		}
		if (sortOrders.isEmpty()) {
		    return defaultCompare(o1, o2);
		}
		for (final Pair<Integer, Ordering> sortingParam : sortOrders) {
		    final Comparable<?> value1 = (Comparable) o1.getValueAt(sortingParam.getKey().intValue() + 1);
		    final Comparable<?> value2 = (Comparable) o2.getValueAt(sortingParam.getKey().intValue() + 1);
		    int result = 0;
		    if (value1 == null) {
			if (value2 != null) {
			    return -1;
			}
		    } else {
			if (value2 == null) {
			    return 1;
			} else {
			    result = compareValues(value1, value2, sortingParam.getValue());
			}
		    }
		    if (result != 0) {
			return result;
		    }
		}
		return defaultCompare(o1, o2);
	    }

	    @SuppressWarnings({ "rawtypes", "unchecked" })
	    private int compareValues(final Comparable value1, final Comparable value2, final Ordering sortingParam) {
		final int sortMultiplier = sortingParam == Ordering.ASCENDING ? 1 : (sortingParam == Ordering.DESCENDING ? -1 : 0);
		return value1.compareTo(value2) * sortMultiplier;
	    }

	    private int defaultCompare(final MutableTreeTableNode o1, final MutableTreeTableNode o2) {
		if (o1.getUserObject().equals(PivotTreeTableNodeEx.NULL_USER_OBJECT)) {
		    if (o2.getUserObject().equals(PivotTreeTableNodeEx.NULL_USER_OBJECT)) {
			return 0;
		    } else {
			return -1;
		    }
		} else {
		    if (o2.getUserObject().equals(PivotTreeTableNodeEx.NULL_USER_OBJECT)) {
			return 1;
		    } else {
			return o1.getUserObject().toString().compareTo(o2.getUserObject().toString());
		    }
		}

	    }

	    private int getIndex(final String string, final List<String> properties) {
		for (int index = 0; index < properties.size(); index++) {
		    final String anotherAggregation = properties.get(index);
		    if (anotherAggregation.equals(string)) {
			return index;
		    }
		}
		return -1;
	    }

	}
    }
}
