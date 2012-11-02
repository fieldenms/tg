package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.ManualEntityCentre;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;

public class ManualCentreConfigurationView<T extends AbstractEntity<?>> extends AbstractConfigurationView<ManualEntityCentre<T>, AbstractWizardView<T>>{

    private static final long serialVersionUID = -1496094602752838088L;

    public ManualCentreConfigurationView(final ManualCentreConfigurationModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ManualCentreConfigurationModel<T> getModel() {
        return (ManualCentreConfigurationModel<T>)super.getModel();
    }

    @Override
    protected ManualEntityCentre<T> createConfigurableView() {
        return new ManualEntityCentre<>(getModel().createEntityCentreModel(), this);
    }

    @Override
    protected AbstractWizardView<T> createWizardView() {
	throw new UnsupportedOperationException("The manual entity centre can not be configured.");
    }
}
