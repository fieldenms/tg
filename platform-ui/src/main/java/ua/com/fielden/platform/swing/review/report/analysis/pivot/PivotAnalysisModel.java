package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.Pair;

public class PivotAnalysisModel<T extends AbstractEntity<?>> extends AbstractAnalysisReviewModel<T, ICentreDomainTreeManagerAndEnhancer, IPivotDomainTreeManagerAndEnhancer, Void> {

    private final PivotTreeTableModel pivotModel;

    public PivotAnalysisModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final IPivotDomainTreeManagerAndEnhancer adtme, final PageHolder pageHolder) {
	super(criteria, adtme, pageHolder);
	pivotModel = new PivotTreeTableModelEx();
    }

    //TODO Provide getConfigurationModel() that returns the specific configuration model for this analysis.T

    @Override
    protected Void executeAnalysisQuery() {
	//TODO Auto-generated method stub
	//TODO Must implement data loading that returns the page and initialises alias map.
	//pivotAnalysisDataProvider.setAliasMap(/*aliasMap*/);
	//pivotAnalysisDataProvider.setLoadedData(/*newPage*/);
	return null;
    }

    public PivotTreeTableModel getPivotModel() {
	return pivotModel;
    }

    @Override
    protected Result canLoadData() {
	// TODO Auto-generated method stub
	return null;
    }

    private class PivotTreeTableModelEx extends PivotTreeTableModel {

	private static final char RIGHT_ARROW = '\u2192';

	private final LinkedHashMap<String, String> categoryAliasMap = new LinkedHashMap<String, String>();
	private final LinkedHashMap<String, String> aggregatedAliasMap = new LinkedHashMap<String, String>();

	private final Comparator<MutableTreeTableNode> sorter = new AggregationSorter();

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
	    final Class<?> managedType = adtme().getEnhancer().getManagedType(root);
	    final IPivotAddToAggregationTickManager secondTick = adtme().getSecondTick();
	    final String property = secondTick.usedProperties(root).get(column - 1);
	    return PropertyTypeDeterminator.determineClass(managedType, property, true, true);
	}

	@Override
	public String getColumnName(final int column) {
	    if (column == 0) {
		String name = "";
		for (final String columnEntry : categoryAliasMap.keySet()) {
		    name += RIGHT_ARROW + columnEntry;
		}
		return name.isEmpty() ? name : name.substring(1);
	    }
	    final Class<T> root = getCriteria().getEntityClass();
	    final IPivotAddToAggregationTickManager secondTick = adtme().getSecondTick();
	    final String property = secondTick.usedProperties(root).get(column - 1);
	    return property;
	}

	@Override
	String getColumnTooltipAt(final int column) {
	    return getColumnName(column);
	}

	@Override
	List<String> categoryProperties() {
	    return Collections.unmodifiableList(new ArrayList<String>(categoryAliasMap.keySet()));
	}

	@Override
	List<String> aggregatedProperties() {
	    return Collections.unmodifiableList(new ArrayList<String>(aggregatedAliasMap.keySet()));
	}

	private final void loadData(final Map<String, List<EntityAggregates>> loadedData, final LinkedHashMap<String, String> categoryAliasMap, final LinkedHashMap<String, String> aggregatedAliasMap){

	    //Loading the alias maps for the aggregation and category properties.
	    if(categoryAliasMap == null || aggregatedAliasMap == null){
		return;
	    }
	    this.categoryAliasMap.clear();
	    this.categoryAliasMap.putAll(categoryAliasMap);
	    this.aggregatedAliasMap.clear();
	    this.aggregatedAliasMap.putAll(aggregatedAliasMap);

	    final PivotTreeTableNodeEx root = new PivotTreeTableNodeEx("root", new EntityAggregates());
	    final PivotTreeTableNodeEx grand = new PivotTreeTableNodeEx("Grand total", loadedData.get("Grand total").get(0));

	    final List<String> categories = new ArrayList<String>(categoryAliasMap.keySet());
	    final Map<Object, Pair<PivotTreeTableNodeEx,Map<?, ?>>> nodes = new HashMap<Object, Pair<PivotTreeTableNodeEx,Map<?, ?>>>();
	    for(int categoryIndex = 0; categoryIndex < categories.size(); categoryIndex++){
		final List<EntityAggregates> listToLoad = loadedData.get(categories.get(categoryIndex));
		for(final EntityAggregates data : listToLoad){
		    Map<Object, Pair<PivotTreeTableNodeEx,Map<?, ?>>> currentLevel = nodes;
		    PivotTreeTableNodeEx currentNode = grand;
		    for(int dataIndex = 0; dataIndex < categoryIndex; dataIndex++){
			final Object key = data.get(categoryAliasMap.get(categories.get(dataIndex)));
			final Pair<PivotTreeTableNodeEx,Map<?, ?>> nodePair = currentLevel.get(key);
			if(nodePair == null){
			    throw new IllegalStateException("The node with " + key + " wasn't found!");
			}
			currentLevel = (Map<Object, Pair<PivotTreeTableNodeEx,Map<?, ?>>>)nodePair.getValue();
			currentNode = nodePair.getKey();
		    }
		    final Object currentKey = data.get(categoryAliasMap.get(categories.get(categoryIndex)));
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

	    private final EntityAggregates aggregatedData;

	    public PivotTreeTableNodeEx(final Object userObject, final EntityAggregates aggregatedData){
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
			final AbstractEntity entity = (AbstractEntity) getUserObject();
			return entity.getKey().toString() + " - " + entity.getDesc();
		    }
		    return getUserObject();
		}
		final Class<T> root = getCriteria().getEntityClass();
		final IPivotAddToAggregationTickManager secondTick = adtme().getSecondTick();
		final String property = secondTick.usedProperties(root).get(column - 1);
		return aggregatedData.get(aggregatedAliasMap.get(property));
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

    @Override
    protected void exportData(final String fileName) throws IOException {
	// TODO Auto-generated method stub

    }

    @Override
    protected String[] getExportFileExtensions() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected String getDefaultExportFileExtension() {
	// TODO Auto-generated method stub
	return null;
    }
}
