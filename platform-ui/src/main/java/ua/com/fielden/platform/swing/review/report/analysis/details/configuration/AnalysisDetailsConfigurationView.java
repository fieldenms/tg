package ua.com.fielden.platform.swing.review.report.analysis.details.configuration;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.details.AnalysisDetailsEntityCentre;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;

public class AnalysisDetailsConfigurationView<T extends AbstractEntity<?>> extends AbstractConfigurationView<AnalysisDetailsEntityCentre<T>, AbstractWizardView<T>> {

    private static final long serialVersionUID = 320053049450359443L;

    public AnalysisDetailsConfigurationView(final AnalysisDetailsConfigurationModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AnalysisDetailsConfigurationModel<T> getModel() {
        return (AnalysisDetailsConfigurationModel<T>)super.getModel();
    }

    @Override
    protected AnalysisDetailsEntityCentre<T> createConfigurableView() {
	return new AnalysisDetailsEntityCentre<>(getModel().createEntityCentreModel(), this);
    }

    @Override
    protected AbstractWizardView<T> createWizardView() {
	throw new UnsupportedOperationException("The analysis details can not be configured.");
    }

}
