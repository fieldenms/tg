package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.EmptyEntityCentre;

public class EmptyCentreConfigurationView<T extends AbstractEntity<?>> extends CentreConfigurationWithoutCriteriaView<T> {

    private static final long serialVersionUID = -2270752656663420832L;

    public EmptyCentreConfigurationView(final CentreConfigurationWithoutCriteriaModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @Override
    protected EmptyEntityCentre<T> createConfigurableView() {
	return new EmptyEntityCentre<>(getModel().createEntityCentreModel(), this);
    }
}
