package ua.com.fielden.platform.swing.review.report.analysis.chart.configuration;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ChartAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.wizard.AnalysisWizardView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

public class ChartAnalysisConfigurationView<T extends AbstractEntity> extends AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, IAnalysisDomainTreeManager, Void, ChartAnalysisView<T>, AnalysisWizardView<T, ICentreDomainTreeManagerAndEnhancer>> {

    private static final long serialVersionUID = -44217633254876740L;

    public ChartAnalysisConfigurationView(final ChartAnalysisConfigurationModel<T> model, final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, owner, progressLayer);
    }

    @Override
    public ChartAnalysisConfigurationModel<T> getModel() {
	return (ChartAnalysisConfigurationModel<T>)super.getModel();
    }

    @Override
    protected ChartAnalysisView<T> createConfigurableView() {
	return new ChartAnalysisView<T>(getModel().createChartAnalysisModel(), getProgressLayer(), getOwner());
    }

    @Override
    protected AnalysisWizardView<T, ICentreDomainTreeManagerAndEnhancer> createWizardView() {
	return new AnalysisWizardView<T, ICentreDomainTreeManagerAndEnhancer>(getOwner(), getModel().createDomainTreeEditorModel(), getProgressLayer());
    }

}
