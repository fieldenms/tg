package ua.com.fielden.platform.swing.review.report.analysis.view;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReview;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.utils.Pair;

public abstract class AbstractAnalysisReview<T extends AbstractEntity, DTM extends ICentreDomainTreeManager, ADTM extends IAbstractAnalysisDomainTreeManager, LDT> extends AbstractEntityReview<T, DTM> {

    private static final long serialVersionUID = -1195915524813089236L;

    private final AbstractEntityCentre<T, DTM> owner;

    private final Action loadAction;
    private final Action exportAction;

    public AbstractAnalysisReview(final AbstractAnalysisReviewModel<T, DTM, ADTM, LDT> model, final BlockingIndefiniteProgressLayer progressLayer, final AbstractEntityCentre<T, DTM> owner) {
	super(model, progressLayer);
	this.owner = owner;
	this.loadAction = createLoadAction();
	this.exportAction = createExportAction();
    }

    public AbstractEntityCentre<T, DTM> getOwner() {
	return owner;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractAnalysisReviewModel<T, DTM, ADTM, LDT> getModel() {
	return (AbstractAnalysisReviewModel<T, DTM, ADTM, LDT>)super.getModel();
    }

    private Action createLoadAction() {
	return new BlockingLayerCommand<Pair<Result,LDT>>("Run", getProgressLayer()) {
	    private static final long serialVersionUID = 1L;

	    {
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
		putValue(Action.SHORT_DESCRIPTION, "Execute query");
		setEnabled(true);
	    }

	    @Override
	    protected boolean preAction() {
		getProgressLayer().enableIncrementalLocking();
		setMessage("Loading...");
		final boolean result = super.preAction();
		if (!result) {
		    return result;
		}
		getOwner().getCriteriaPanel().updateModel();
		enableRelatedActions(false, false);
		return true;
	    }

	    @Override
	    protected Pair<Result,LDT> action(final ActionEvent e) throws Exception {
		final Result result = getModel().canLoadData();
		if(result.isSuccessful()){
		    return new Pair<Result, LDT>(result, getModel().executeAnalysisQuery());
		}
		return new Pair<Result, LDT>(result, null);
	    }

	    @Override
	    protected void postAction(final Pair<Result,LDT> result) {
		if (!result.getKey().isSuccessful()) {
		    JOptionPane.showMessageDialog(AbstractAnalysisReview.this, result.getKey().getMessage());
		}
		//		else {
		//		    setDataToView(result.getValue()); // note that currently setting data to view and updating buttons state etc. perform in this single IReviewContract implementor method.
		//		}
		enableRelatedActions(true, false);
		super.postAction(result);
	    }

	    /**
	     * After default exception handling executed, post-actions should be performed to enable all necessary buttons, unlock layer etc.
	     */
	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);

		enableRelatedActions(true, false);
		super.postAction(null);
	    }
	};

    }

    private Action createExportAction() {
	// TODO Auto-generated method stub
	return null;
    }

    public void loadData(){
	loadAction.actionPerformed(null);
    }

    public void exportData(){
	exportAction.actionPerformed(null);
    }

    /**
     * Enables or disables actions related to this analysis (run, export, paginator actions e.t.c.). The second parameter determines
     * whether this method was invoked after page navigation or after the data loading.
     * 
     * @param enable
     * @param navigate
     */
    abstract protected void enableRelatedActions(final boolean enable, final boolean navigate);
    //    {
    //	if(!enable){
    //	    getOwner().getDefaultAction().setEnabled(false);
    //	    if(getOwner().getCriteriaPanel().canConfigure()){
    //		getOwner().getCriteriaPanel().getSwitchAction().setEnabled(false);
    //	    }
    //	    if(getOwner().getCustomActionChanger() != null){
    //		getOwner().getCustomActionChanger().setEnabled(false);
    //	    }
    //	    if(!navigate){
    //		getOwner().getPaginator().disableActions();
    //	    }
    //	    getOwner().getPaginator().disableActions();
    //	    getOwner().getExportAction().setEnabled(false);
    //	    getOwner().getRunAction().setEnabled(false);
    //	}
    //    }

    //    public PageHolder<AbstractEntity> getPageHolder(){
    //	return pageHolder;
    //    }

}
