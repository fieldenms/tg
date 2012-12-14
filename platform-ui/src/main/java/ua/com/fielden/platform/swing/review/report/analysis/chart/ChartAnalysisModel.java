package ua.com.fielden.platform.swing.review.report.analysis.chart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.report.query.generation.AnalysisResultClassBundle;
import ua.com.fielden.platform.report.query.generation.ChartAnalysisQueryGenerator;
import ua.com.fielden.platform.report.query.generation.IReportQueryGenerator;
import ua.com.fielden.platform.reportquery.AnalysisModelChangedEvent;
import ua.com.fielden.platform.swing.pagination.model.development.IPageChangedListener;
import ua.com.fielden.platform.swing.pagination.model.development.PageChangedEvent;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.types.Money;

public class ChartAnalysisModel<T extends AbstractEntity<?>> extends AbstractAnalysisReviewModel<T, ICentreDomainTreeManagerAndEnhancer, IAnalysisDomainTreeManager> {

    private final ChartAnalysisDataProvider<T> chartAnalysisDataProvider = new ChartAnalysisDataProvider<T>();

    public ChartAnalysisModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final IAnalysisDomainTreeManager adtme) {
	super(criteria, adtme);
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
    protected ChartAnalysisView<T> getAnalysisView() {
        return (ChartAnalysisView<T>)super.getAnalysisView();
    }

    private Result canLoadData() {
	final Result result = getCriteria().isValid();
	if(!result.isSuccessful()){
	    return result;
	}
	final Class<T> entityClass = getCriteria().getEntityClass();
	if(adtme().getFirstTick().usedProperties(entityClass).isEmpty()){
	    return new Result(new IllegalStateException("Please choose distribution property"));
	}
	return Result.successful(this);
    }

    @Override
    protected Result executeAnalysisQuery() {
	final Result analysisQueryExecutionResult = canLoadData();
	if(!analysisQueryExecutionResult.isSuccessful()){
	    return analysisQueryExecutionResult;
	}

	final Class<T> root = getCriteria().getEntityClass();

	final IReportQueryGenerator<T> chartAnalysisQueryGenerator = new ChartAnalysisQueryGenerator<>(//
		root, //
		getCriteria().getCentreDomainTreeMangerAndEnhancer(), //
		adtme());

	final List<String> distributionProperties = adtme().getFirstTick().usedProperties(root);
	final List<String> aggregationProperties = adtme().getSecondTick().usedProperties(root);

	final AnalysisResultClassBundle<T> classBundle = chartAnalysisQueryGenerator.generateQueryModel();

	final IPage<T> result = getCriteria().run(classBundle.getQueries().get(0), classBundle.getGeneratedClass(), classBundle.getGeneratedClassRepresentation(), getAnalysisView().getPageSize());
	chartAnalysisDataProvider.setUsedProperties(distributionProperties, aggregationProperties);
	getPageHolder().newPage(result);
	return Result.successful(result);
    }

    @Override
    protected Result exportData(final String fileName) throws IOException {
	return new Result(new UnsupportedOperationException("Chart analysis doesnt supports data exporting!"));
    }

    @Override
    protected String[] getExportFileExtensions() {
	throw new UnsupportedOperationException("Chart analysis doesnt supports data exporting!");    }

    @Override
    protected String getDefaultExportFileExtension() {
	throw new UnsupportedOperationException("Chart analysis doesnt supports data exporting!");    }

    private static class ChartAnalysisDataProvider<T extends AbstractEntity<?>> extends AbstractCategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> {

	//Category properties and it's aliases.
	private final List<String> categoryList = new ArrayList<>();
	private final List<String> aggregationList = new ArrayList<>();

	private final List<T> loadedData = new ArrayList<>();

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
	    if(value == null){
		return null;
	    }else if (value instanceof Money) {
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
	private void setUsedProperties(final List<String> categoryList, final List<String> aggregationList){
	    if(categoryList == null || aggregationList == null){
		return;
	    }
	    this.categoryList.clear();
	    this.categoryList.addAll(categoryList);
	    this.aggregationList.clear();
	    this.aggregationList.addAll(aggregationList);
	}

	@Override
	public List<String> aggregatedProperties() {
	    return Collections.unmodifiableList(aggregationList);
	}

	@Override
	public List<String> categoryProperties() {
	    return Collections.unmodifiableList(categoryList);
	}
    }
}
