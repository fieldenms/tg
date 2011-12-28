package ua.com.fielden.platform.swing.review.report.centre;

import javax.swing.JComponent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationPanel;

public class SingleAnalysisEntityCentre<T extends AbstractEntity> extends AbstractEntityCentre<T> {

    private static final long serialVersionUID = -4025190200012481751L;

    public SingleAnalysisEntityCentre(final EntityCentreModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
	createReview();
	layoutComponents();
    }

    private void createReview() {
	final BlockingIndefiniteProgressLayer reviewProgressLayer = getReviewProgressLayer();
	final GridConfigurationModel<T> configModel = new GridConfigurationModel<T>(getModel().getCriteria());
	final GridConfigurationPanel<T> gridConfigView = new GridConfigurationPanel<T>("Main details", configModel, this, reviewProgressLayer);
	reviewProgressLayer.setView(gridConfigView);
	gridConfigView.open();
	setCurrentAnalysisConfigurationView(gridConfigView);
    }

    @Override
    public JComponent getReviewPanel() {
	return getReviewProgressLayer();
    }
}
