package ua.com.fielden.platform.swing.review.report.analysis.customiser;

import ua.com.fielden.platform.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.report.query.generation.ChartAnalysisQueryGenerator;
import ua.com.fielden.platform.report.query.generation.IReportQueryGeneration;
import ua.com.fielden.platform.swing.review.details.IDetails;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ChartAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ChartAnalysisView;

public class DefaultChartAnalysisCustomiser<E extends AbstractEntity<?>> implements IAnalysisCustomiser<ChartAnalysisView<E>> {

    @Override
    public ActionPanelBuilder createToolBar(final ChartAnalysisView<E> analysisView) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <DT> IDetails<DT> getDetails(final Class<DT> detailsParamType) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <T extends AbstractEntity<?>> IReportQueryGeneration<T> getQueryGenerator(final ChartAnalysisView<E> analysisView, final Class<T> queryClass) {
	final ChartAnalysisModel<E> analysisModel = analysisView.getModel();
	final IReportQueryGeneration<T> chartAnalysisQueryGenerator = new ChartAnalysisQueryGenerator<>(//
		queryClass, //
		analysisModel.getCriteria().getCentreDomainTreeMangerAndEnhancer(), //
		analysisModel.adtme());
	return chartAnalysisQueryGenerator;
    }

}
