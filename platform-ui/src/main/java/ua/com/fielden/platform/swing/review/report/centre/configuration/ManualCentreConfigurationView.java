package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.ManualEntityCentre;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;

public class ManualCentreConfigurationView<T extends AbstractEntity<?>, M extends AbstractEntity<?>> extends AbstractConfigurationView<ManualEntityCentre<T, M>, AbstractWizardView<T>>{

    private static final long serialVersionUID = -1496094602752838088L;

    public ManualCentreConfigurationView(final ManualCentreConfigurationModel<T, M> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ManualCentreConfigurationModel<T, M> getModel() {
        return (ManualCentreConfigurationModel<T, M>) super.getModel();
    }

    @Override
    protected ManualEntityCentre<T, M> createConfigurableView() {
        return new ManualEntityCentre<>(getModel().createEntityCentreModel(), this);
    }

    @Override
    protected AbstractWizardView<T> createWizardView() {
	throw new UnsupportedOperationException("The manual entity centre can not be configured.");
    }
}
