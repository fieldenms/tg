package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IMultipleDecDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.report.query.generation.AnalysisResultClassBundle;
import ua.com.fielden.platform.report.query.generation.IReportQueryGenerator;
import ua.com.fielden.platform.report.query.generation.MultipleDecAnalysisQueryGenerator;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.chart.GroupAnalysisDataProvider;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ICategoryAnalysisDataProvider;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

public class MultipleDecModel<T extends AbstractEntity<?>> extends AbstractAnalysisReviewModel<T, ICentreDomainTreeManagerAndEnhancer, IMultipleDecDomainTreeManager> {

    private final GroupAnalysisDataProvider<T> multipleDecDataProvider = new GroupAnalysisDataProvider<T>();

    public MultipleDecModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final IMultipleDecDomainTreeManager adtme) {
	super(criteria, adtme);
    }

    @Override
    protected Result executeAnalysisQuery() {
	final Result analysisQueryExecutionResult = canLoadData();
	if(!analysisQueryExecutionResult.isSuccessful()){
	    return analysisQueryExecutionResult;
	}

	final Class<T> root = getCriteria().getEntityClass();

	final IReportQueryGenerator<T> chartAnalysisQueryGenerator = new MultipleDecAnalysisQueryGenerator<>(//
		root, //
		getCriteria().getCentreDomainTreeMangerAndEnhancer(), //
		adtme());

	final List<String> distributionProperties = adtme().getFirstTick().usedProperties(root);
	final List<String> aggregationProperties = adtme().getSecondTick().usedProperties(root);

	final AnalysisResultClassBundle<T> classBundle = chartAnalysisQueryGenerator.generateQueryModel();

	final List<T> result = getCriteria().run(classBundle.getQueries().get(0), classBundle.getGeneratedClass(), classBundle.getGeneratedClassRepresentation());
	multipleDecDataProvider.setUsedProperties(distributionProperties, aggregationProperties);
	SwingUtilitiesEx.invokeLater(new Runnable() {

		@Override
		public void run() {
		    multipleDecDataProvider.setUsedProperties(distributionProperties, aggregationProperties);
		    multipleDecDataProvider.setLoadedData(result);
		}
	    });
	return Result.successful(result);
    }

    public ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> getChartModel() {
	return multipleDecDataProvider;
    }

    public ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> getMultipleDecDataProvider() {
	return multipleDecDataProvider;
    }

    @Override
    protected MultipleDecView<T> getAnalysisView() {
        return (MultipleDecView<T>)super.getAnalysisView();
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
    protected Result exportData(final String fileName) throws IOException {
	return new Result(new UnsupportedOperationException("Chart analysis doesnt supports data exporting!"));
    }

    @Override
    protected String[] getExportFileExtensions() {
	throw new UnsupportedOperationException("Chart analysis doesnt supports data exporting!");    }

    @Override
    protected String getDefaultExportFileExtension() {
	throw new UnsupportedOperationException("Chart analysis doesnt supports data exporting!");    }
}
