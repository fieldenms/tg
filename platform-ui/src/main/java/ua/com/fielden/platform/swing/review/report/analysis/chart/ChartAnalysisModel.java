package ua.com.fielden.platform.swing.review.report.analysis.chart;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reportquery.AnalysisModelChangedEvent;
import ua.com.fielden.platform.swing.pagination.model.development.IPageChangedListener;
import ua.com.fielden.platform.swing.pagination.model.development.PageChangedEvent;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.DynamicFetchBuilder;
import ua.com.fielden.platform.swing.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

public class ChartAnalysisModel<T extends AbstractEntity<?>> extends AbstractAnalysisReviewModel<T, ICentreDomainTreeManagerAndEnhancer ,IAnalysisDomainTreeManager, Void> {

    private final ChartAnalysisDataProvider<T> chartAnalysisDataProvider = new ChartAnalysisDataProvider<T>();

    private ChartAnalysisView<T> analysisView;

    public ChartAnalysisModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final IAnalysisDomainTreeManager adtme, final PageHolder pageHolder) {
	super(criteria, adtme, pageHolder);
	this.analysisView = null;
	getPageHolder().addPageChangedListener(new IPageChangedListener() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public void pageChanged(final PageChangedEvent e) {
		chartAnalysisDataProvider.setLoadedPage((IPage<T>)e.getNewPage());
	    }
	});
    }

    public ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> getChartAnalysisDataProvider() {
	return chartAnalysisDataProvider;
    }

    @Override
    protected Result canLoadData() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected Void executeAnalysisQuery() {
	final Class<T> root = getCriteria().getEntityClass();
	final List<String> distributionProperties = adtme().getFirstTick().usedProperties(root);
	final List<String> aggregationProperties = adtme().getSecondTick().usedProperties(root);

	ICompleted<T> baseQuery = DynamicQueryBuilder.createQuery(getCriteria().getManagedType(), getCriteria().createQueryProperties());
	for (final String groupProperty : distributionProperties) {
	    baseQuery = getCriteria().groupBy(groupProperty, baseQuery);
	}
	final List<String> yieldProperties = new ArrayList<String>();
	yieldProperties.addAll(distributionProperties);
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
	    for(final String groupOrder : distributionProperties){
		orderingProperties.add(new Pair<String, Ordering>(groupOrder, Ordering.ASCENDING));
	    }
	}
	final List<Pair<Object, Ordering>> orderingPairs = EntityQueryCriteriaUtils.getOrderingList(root, //
		orderingProperties, //
		getCriteria().getCentreDomainTreeMangerAndEnhancer().getEnhancer());
	final QueryExecutionModel<T, EntityResultQueryModel<T>> resultQuery = from(queryModel)
	.with(DynamicOrderingBuilder.createOrderingModel(getCriteria().getManagedType(), orderingPairs))//
	.with(DynamicFetchBuilder.createFetchModel(getCriteria().getManagedType(), new HashSet<String>(distributionProperties))).model();

	final IPage<T> result = getCriteria().run(resultQuery, analysisView.getPageSize());
	chartAnalysisDataProvider.setUsedProperties(distributionProperties, aggregationProperties);
	getPageHolder().newPage(result);
	return null;
    }

    /**
     * Set the analysis view for this model. Throws {@link IllegalStateException} if the model was already set.
     *
     * @param analysisView
     */
    final void setAnalysisView(final ChartAnalysisView<T> analysisView){
	if(this.analysisView != null){
	    throw new IllegalStateException("The analysis view can be set only once!");
	}
	this.analysisView = analysisView;
    }

    private static class ChartAnalysisDataProvider<T extends AbstractEntity<?>> extends AbstractCategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> {

	private final List<String> categoryAliasMap = new ArrayList<String>();
	private final List<String> aggregatedAliasMap = new ArrayList<String>();
	private final List<T> loadedData = new ArrayList<T>();

	@Override
	public int getCategoryDataEntryCount() {
	    return loadedData.size();
	}

	@Override
	public Comparable<?> getCategoryDataValue(final int index, final String category) {
	    return (Comparable<?>)loadedData.get(index).get(category);
	}

	@Override
	public Number getAggregatedDataValue(final int index, final String aggregated) {
	    final Object value = loadedData.get(index).get(aggregated);
	    if (value instanceof Money) {
		return ((Money) value).getAmount();
	    } else if (value instanceof Number) {
		return (Number) value;
	    }
	    throw new IllegalArgumentException("The value type is " + value.getClass().getSimpleName() + " please make sure that the passed parameters are correct\n" +
		    "The index is: " + index + ". The aggregation property is: " + aggregated + ".");
	}

	@Override
	public List<T> getLoadedData() {
	    return loadedData;
	}

	/**
	 * Set the loaded page and data.
	 *
	 * @param loadedPage
	 */
	private void setLoadedPage(final IPage<T> loadedPage){
	    if(loadedPage != null ){
		this.loadedData.clear();
		this.loadedData.addAll(loadedPage.data());
		fireAnalysisModelChangeEvent(new AnalysisModelChangedEvent(this));
	    }
	}

	/**
	 * Set the map that associates the aggregated and grouped by properties with their aliases.
	 *
	 * @param aliasMap
	 */
	private void setUsedProperties(final List<String> categoryAliasMap, final List<String> aggregatedAliasMap){
	    if(categoryAliasMap == null || aggregatedAliasMap == null){
		return;
	    }
	    this.categoryAliasMap.clear();
	    this.categoryAliasMap.addAll(categoryAliasMap);
	    this.aggregatedAliasMap.clear();
	    this.aggregatedAliasMap.addAll(aggregatedAliasMap);
	}

	@Override
	public List<String> aggregatedProperties() {
	    return Collections.unmodifiableList(aggregatedAliasMap);
	}

	@Override
	public List<String> categoryProperties() {
	    return Collections.unmodifiableList(categoryAliasMap);
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
