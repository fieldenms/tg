package ua.com.fielden.platform.swing.review.report.analysis.chart;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.pagination.IPageChangedListener;
import ua.com.fielden.platform.pagination.PageChangedEvent;
import ua.com.fielden.platform.report.query.generation.AnalysisResultClassBundle;
import ua.com.fielden.platform.report.query.generation.ChartAnalysisQueryGenerator;
import ua.com.fielden.platform.report.query.generation.IReportQueryGenerator;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

public class ChartAnalysisModel<T extends AbstractEntity<?>> extends AbstractAnalysisReviewModel<T, ICentreDomainTreeManagerAndEnhancer, IAnalysisDomainTreeManager> {

    private final GroupAnalysisDataProvider<T> groupAnalysisDataProvider = new GroupAnalysisDataProvider<T>();

    public ChartAnalysisModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final IAnalysisDomainTreeManager adtme) {
	super(criteria, adtme);
	getPageHolder().addPageChangedListener(new IPageChangedListener() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public void pageChanged(final PageChangedEvent e) {
		SwingUtilitiesEx.invokeLater(new Runnable() {

		    @Override
		    public void run() {
			groupAnalysisDataProvider.setLoadedData(((IPage<T>)e.getNewPage()).data());
			if (getAnalysisView() != null) {
			    getAnalysisView().setVisibleCategoryCount(groupAnalysisDataProvider.getCategoryDataEntryCount());
			}
		    }
		});
	    }
	});
    }

    public ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> getChartAnalysisDataProvider() {
	return groupAnalysisDataProvider;
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

	final IPage<T> result = getCriteria().run(classBundle.getQueries().get(0).composeQuery(), classBundle.getGeneratedClass(), classBundle.getGeneratedClassRepresentation(), getAnalysisView().getPageSize());
	groupAnalysisDataProvider.setUsedProperties(distributionProperties, aggregationProperties);
	getPageHolder().newPage(result);
	return Result.successful(result);
    }

    @Override
    protected Result exportData(final String fileName) throws IOException {
	return new Result(new UnsupportedOperationException("Chart analysis doesn't supports data exporting!"));
    }

    @Override
    protected String[] getExportFileExtensions() {
	throw new UnsupportedOperationException("Chart analysis doesn't supports data exporting!");
    }

    @Override
    protected String getDefaultExportFileExtension() {
	throw new UnsupportedOperationException("Chart analysis doesn't supports data exporting!");    }
}
