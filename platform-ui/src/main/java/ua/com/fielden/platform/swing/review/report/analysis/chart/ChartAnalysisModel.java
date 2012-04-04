package ua.com.fielden.platform.swing.review.report.analysis.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reportquery.AnalysisModelChangedEvent;
import ua.com.fielden.platform.swing.pagination.model.development.IPageChangedListener;
import ua.com.fielden.platform.swing.pagination.model.development.PageChangedEvent;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.types.Money;

public class ChartAnalysisModel<T extends AbstractEntity<?>> extends AbstractAnalysisReviewModel<T, ICentreDomainTreeManagerAndEnhancer ,IAnalysisDomainTreeManager, Void> {

    private final ChartAnalysisDataProvider chartAnalysisDataProvider = new ChartAnalysisDataProvider();

    public ChartAnalysisModel(final AbstractAnalysisConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> configurationModel, final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final IAnalysisDomainTreeManager adtm, final PageHolder pageHolder) {
	super(configurationModel, criteria, adtm, pageHolder);
	getPageHolder().addPageChangedListener(new IPageChangedListener() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public void pageChanged(final PageChangedEvent e) {
		chartAnalysisDataProvider.setLoadedPage((IPage<EntityAggregates>)e.getNewPage());
	    }
	});
    }

    //TODO Provide getConfigurationModel() that returns the specific configuration model for this analysis.


    public ICategoryAnalysisDataProvider<Comparable<?>, Number, IPage<EntityAggregates>> getChartAnalysisDataProvider() {
	return chartAnalysisDataProvider;
    }

    @Override
    protected Result canLoadData() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected Void executeAnalysisQuery() {
	// TODO Auto-generated method stub
	//TODO Must implement data loading that returns the page and initialises alias map.
	//chartAnalysisDataProvider.setAliasMap(aliasMap, aliasMap1);
	//getPageHolder().newPage(/*newPage*/);
	return null;
    }

    private static class ChartAnalysisDataProvider extends AbstractCategoryAnalysisDataProvider<Comparable<?>, Number, IPage<EntityAggregates>> {

	private final LinkedHashMap<String, String> categoryAliasMap = new LinkedHashMap<String, String>();
	private final LinkedHashMap<String, String> aggregatedAliasMap = new LinkedHashMap<String, String>();

	private IPage<EntityAggregates> loadedPage;
	private List<EntityAggregates> loadedData;

	@Override
	public int getCategoryDataEntryCount() {
	    return loadedData.size();
	}

	@Override
	public Comparable<?> getCategoryDataValue(final int index, final String category) {
	    return (Comparable<?>)loadedData.get(index).get(categoryAliasMap.get(category));
	}

	@Override
	public Number getAggregatedDataValue(final int index, final String aggregated) {
	    final Object value = loadedData.get(index).get(aggregatedAliasMap.get(aggregated));
	    if (value instanceof Money) {
		return ((Money) value).getAmount();
	    } else if (value instanceof Number) {
		return (Number) value;
	    }
	    throw new IllegalArgumentException("The value type is " + value.getClass().getSimpleName() + " please make sure that the passed parameters are correct\n" +
		    "The index is: " + index + ". The aggregation property is: " + aggregated + ".");
	}

	@Override
	public IPage<EntityAggregates> getLoadedData() {
	    return loadedPage;
	}

	/**
	 * Set the loaded page and data.
	 *
	 * @param loadedPage
	 */
	private void setLoadedPage(final IPage<EntityAggregates> loadedPage){
	    if(loadedPage != null ){
		this.loadedPage = loadedPage;
		this.loadedData = loadedPage.data();
		fireAnalysisModelChangeEvent(new AnalysisModelChangedEvent(this));
	    }
	}

	/**
	 * Set the map that associates the aggregated and grouped by properties with their aliases.
	 *
	 * @param aliasMap
	 */
	private void setAliasMap(final LinkedHashMap<String, String> categoryAliasMap, final LinkedHashMap<String, String> aggregatedAliasMap){
	    if(categoryAliasMap == null || aggregatedAliasMap == null){
		return;
	    }
	    this.categoryAliasMap.clear();
	    this.categoryAliasMap.putAll(categoryAliasMap);
	    this.aggregatedAliasMap.clear();
	    this.aggregatedAliasMap.putAll(aggregatedAliasMap);
	}

	@Override
	public List<String> aggregatedProperties() {
	    return Collections.unmodifiableList(new ArrayList<String>(aggregatedAliasMap.keySet()));
	}

	@Override
	public List<String> categoryProperties() {
	    return Collections.unmodifiableList(new ArrayList<String>(categoryAliasMap.keySet()));
	}
    }
}
