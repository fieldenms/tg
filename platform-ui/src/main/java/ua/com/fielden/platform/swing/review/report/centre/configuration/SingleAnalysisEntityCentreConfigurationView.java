package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.SingleAnalysisEntityCentre;

public class SingleAnalysisEntityCentreConfigurationView<T extends AbstractEntity> extends AbstractCentreConfigurationView<T, SingleAnalysisEntityCentre<T>> {

    private static final long serialVersionUID = -3749891053466125465L;

    public SingleAnalysisEntityCentreConfigurationView(final CentreConfigurationModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @Override
    protected SingleAnalysisEntityCentre<T> createConfigurableView() {
	return new SingleAnalysisEntityCentre<T>(getModel().createEntityCentreModel(), getProgressLayer());
    }

}
