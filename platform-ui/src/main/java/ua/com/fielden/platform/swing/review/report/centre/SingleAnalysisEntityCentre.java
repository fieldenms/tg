package ua.com.fielden.platform.swing.review.report.centre;

import javax.swing.Action;
import javax.swing.JComponent;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;

public class SingleAnalysisEntityCentre<T extends AbstractEntity> extends AbstractSingleAnalysisEntityCentre<T, ICentreDomainTreeManager> {

    private static final long serialVersionUID = -4025190200012481751L;

    public SingleAnalysisEntityCentre(final EntityCentreModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
	createReview();
	layoutComponents();
    }

    @Override
    public JComponent getReviewPanel() {
	return getReviewProgressLayer();
    }

    @Override
    protected Action createSaveAsDefaultAction() {
	return null;
    }

    @Override
    protected Action createLoadDefaultAction() {
	return null;
    }

    @Override
    protected Action createRemoveAction() {
	return getModel().getName() == null ? null : super.createRemoveAction();
    }
}
