package ua.com.fielden.platform.swing.review.report.centre;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.report.centre.configuration.SingleAnalysisEntityCentreConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;

public class SingleAnalysisEntityCentre<T extends AbstractEntity<?>> extends AbstractSingleAnalysisEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> {

    private static final long serialVersionUID = -4025190200012481751L;

    public SingleAnalysisEntityCentre(final EntityCentreModel<T> model, final SingleAnalysisEntityCentreConfigurationView<T> owner) {
	super(model, owner);
	createReview();
	layoutComponents();
    }

    @Override
    public EntityCentreModel<T> getModel() {
	return (EntityCentreModel<T>)super.getModel();
    }

    @SuppressWarnings("unchecked")
    @Override
    public SingleAnalysisEntityCentreConfigurationView<T> getOwner() {
	return (SingleAnalysisEntityCentreConfigurationView<T>)super.getOwner();
    }

    @Override
    public JComponent getReviewPanel() {
	return getReviewProgressLayer();
    }

    @Override
    protected List<Action> createCustomActionList() {
	final List<Action> customActions = new ArrayList<Action>();
	customActions.add(getConfigureAction());
	customActions.add(getOwner().getSave());
	customActions.add(getOwner().getSaveAs());
	customActions.add(getOwner().getRemove());
	return customActions;
    }

    @Override
    protected ConfigureAction createConfigureAction() {
	return new ConfigureAction(getOwner()) {

	    private static final long serialVersionUID = -1973027500637301627L;

	    {
		putValue(Action.NAME, "Configure");
		putValue(Action.SHORT_DESCRIPTION, "Configure this entity centre");
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		getOwner().getModel().freez();
		return null;
	    }

	    @Override
	    protected void restoreAfterError() {
		if(getOwner().getModel().isFreezed()){
		    getOwner().getModel().discard();
		}
	    }
	};
    }
}
