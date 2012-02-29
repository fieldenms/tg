package ua.com.fielden.platform.swing.review.report.centre;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
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
    public EntityCentreModel<T> getModel() {
	return (EntityCentreModel<T>)super.getModel();
    }

    @Override
    public JComponent getReviewPanel() {
	return getReviewProgressLayer();
    }

    @Override
    protected List<Action> createCustomActionList() {
	final List<Action> customActions = new ArrayList<Action>();
	customActions.add(getConfigureAction());
	customActions.add(createSaveAction());
	customActions.add(createSaveAsAction());
	customActions.add(createRemoveAction());
	return customActions;
    }

    private Action createSaveAction() {
	return new AbstractAction("Save") {

	    private static final long serialVersionUID = 8474884103209307717L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		getModel().getConfigurationModel().save();
	    }
	};
    }

    private Action createSaveAsAction() {
	return new AbstractAction("Save As") {

	    private static final long serialVersionUID = 6870686264834331196L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		getModel().getConfigurationModel().saveAs();
	    }
	};
    }

    private Action createRemoveAction() {
	return getModel().getName() == null ? null : new AbstractAction("Delete") {

	    private static final long serialVersionUID = 8474884103209307717L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		getModel().getConfigurationModel().remove();
	    }
	};
    }
}
