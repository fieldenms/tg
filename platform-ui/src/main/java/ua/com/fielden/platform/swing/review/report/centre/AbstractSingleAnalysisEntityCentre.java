package ua.com.fielden.platform.swing.review.report.centre;

import javax.swing.JComponent;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationPanel;

public abstract class AbstractSingleAnalysisEntityCentre<T extends AbstractEntity, DTM extends ICentreDomainTreeManager> extends AbstractEntityCentre<T, DTM> {

    private static final long serialVersionUID = -7393061848126429375L;

    public AbstractSingleAnalysisEntityCentre(final AbstractEntityCentreModel<T, DTM> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @Override
    public JComponent getReviewPanel() {
	return getReviewProgressLayer();
    }

    final void createReview() {
	final BlockingIndefiniteProgressLayer reviewProgressLayer = getReviewProgressLayer();
	final GridConfigurationModel<T, DTM> configModel = new GridConfigurationModel<T, DTM>(getModel().getCriteria());
	final GridConfigurationPanel<T, DTM> gridConfigView = new GridConfigurationPanel<T, DTM>("Main details", configModel, this, reviewProgressLayer);
	reviewProgressLayer.setView(gridConfigView);
	gridConfigView.open();
	setCurrentAnalysisConfigurationView(gridConfigView);
    }
}
