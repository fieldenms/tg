package ua.com.fielden.platform.swing.review.report.analysis.pivot.configuration;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.pivot.PivotAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.wizard.AnalysisWizardView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

public class PivotAnalysisConfigurationView<T extends AbstractEntity> extends AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, IPivotDomainTreeManager, Void, PivotAnalysisView<T>, AnalysisWizardView<T, ICentreDomainTreeManagerAndEnhancer>> {

    private static final long serialVersionUID = -1464413279095086886L;

    public PivotAnalysisConfigurationView(final PivotAnalysisConfigurationModel<T> model, final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, owner, progressLayer);
    }

    @Override
    public PivotAnalysisConfigurationModel<T> getModel() {
	return (PivotAnalysisConfigurationModel<T>)super.getModel();
    }

    @Override
    protected PivotAnalysisView<T> createConfigurableView() {
	return new PivotAnalysisView<T>(getModel().createPivotAnalysisModel(), getProgressLayer(), getOwner());
    }

    @Override
    protected AnalysisWizardView<T, ICentreDomainTreeManagerAndEnhancer> createWizardView() {
	return new AnalysisWizardView<T, ICentreDomainTreeManagerAndEnhancer>(getOwner(), getModel().createDomainTreeEditorModel(), getProgressLayer());
    }

}
