package ua.com.fielden.platform.swing.review.report.analysis.chart.configuration;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ChartAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.events.AbstractConfigurationViewEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IAbstractConfigurationViewEventListener;

public class ChartAnalysisConfigurationView<T extends AbstractEntity<?>> extends AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, IAnalysisDomainTreeManager, Void, ChartAnalysisView<T>> {

    private static final long serialVersionUID = -44217633254876740L;

    public ChartAnalysisConfigurationView(final ChartAnalysisConfigurationModel<T> model, final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, owner, progressLayer);
	addOpenEventListener(createOpenAnalysisEventListener());
    }

    @Override
    public ChartAnalysisConfigurationModel<T> getModel() {
	return (ChartAnalysisConfigurationModel<T>)super.getModel();
    }

    @Override
    protected ChartAnalysisView<T> createConfigurableView() {
	return new ChartAnalysisView<T>(getModel().createChartAnalysisModel(), this);
    }

    private IAbstractConfigurationViewEventListener createOpenAnalysisEventListener() {
	return new IAbstractConfigurationViewEventListener() {

	    @Override
	    public Result abstractConfigurationViewEventPerformed(final AbstractConfigurationViewEvent event) {
		switch (event.getEventAction()) {
		case OPEN:
		    IAnalysisDomainTreeManager adtme = (IAnalysisDomainTreeManager)getModel().getAnalysisManager();
		    if(adtme == null){
			getModel().initAnalysisManager(AnalysisType.SIMPLE);
			getModel().save();
			getModel().setAnalysisVisible(true);
			adtme = (IAnalysisDomainTreeManager)getModel().getAnalysisManager();
		    }
		    if(adtme == null){
			return new Result(ChartAnalysisConfigurationView.this, new IllegalStateException("The analysis can not be initialized!"));
		    }
		    return getModel().canSetMode(ReportMode.REPORT);

		default:
		    return Result.successful(ChartAnalysisConfigurationView.this);
		}
	    }
	};
    }
}
