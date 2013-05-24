package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToAggregationTickManager;
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
import ua.com.fielden.platform.utils.Pair;

public class MultipleDecModel<T extends AbstractEntity<?>> extends AbstractAnalysisReviewModel<T, ICentreDomainTreeManagerAndEnhancer, IMultipleDecDomainTreeManager> {

    private final GroupAnalysisDataProvider<T> multipleDecDataProvider = new GroupAnalysisDataProvider<T>();
    private final MultipleDecSorter sorter = new MultipleDecSorter();

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

	final List<T> result = getCriteria().run(classBundle.getCdtmeWithWhichAnalysesQueryHaveBeenCreated(), classBundle.getQueries().get(0).composeQuery(), classBundle.getGeneratedClass(), classBundle.getGeneratedClassRepresentation());
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

    public void sortLoadedData(){
	multipleDecDataProvider.sortLoadedData(sorter);
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
	throw new UnsupportedOperationException("Chart analysis doesnt supports data exporting!");
    }

    @Override
    protected String getDefaultExportFileExtension() {
	throw new UnsupportedOperationException("Chart analysis doesnt supports data exporting!");
    }

    private class MultipleDecSorter implements Comparator<T> {

	    @SuppressWarnings("rawtypes")
	    @Override
	    public int compare(final T o1, final T o2) {

		final Class<T> root = getCriteria().getEntityClass();
		final String category = multipleDecDataProvider.categoryProperties().size() != 1 ? null : multipleDecDataProvider.categoryProperties().get(0);
		final IAnalysisAddToAggregationTickManager secondTick = adtme().getSecondTick();

		final List<Pair<String, Ordering>> sortObjects = secondTick.orderedProperties(root);
		if (sortObjects == null || sortObjects.isEmpty()) {
		    return defaultCompare(o1.get(category), o2.get(category));
		}
		for (final Pair<String, Ordering> sortingParam : sortObjects) {
		    final Comparable<?> value1 = (Comparable) o1.get(sortingParam.getKey());
		    final Comparable<?> value2 = (Comparable) o2.get(sortingParam.getKey());
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

	    private int defaultCompare(final Object o1, final Object o2) {
		if (o1 == null) {
		    if (o2 == null) {
			return 0;
		    } else {
			return -1;
		    }
		} else {
		    if (o2 == null) {
			return 1;
		    } else {
			return o1.toString().compareTo(o2.toString());
		    }
		}
	    }
	}
}
