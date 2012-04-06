package ua.com.fielden.platform.swing.review.report.analysis.view;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.pagination.model.development.IPageNavigationListener;
import ua.com.fielden.platform.swing.pagination.model.development.PageNavigationEvent;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReview;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.utils.Pair;

public abstract class AbstractAnalysisReview<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer, ADTME extends IAbstractAnalysisDomainTreeManagerAndEnhancer, LDT> extends AbstractEntityReview<T, CDTME> {

    private static final long serialVersionUID = -1195915524813089236L;

    private final AbstractEntityCentre<T, CDTME> owner;

    private final Action loadAction;
    private final Action exportAction;

    public AbstractAnalysisReview(final AbstractAnalysisReviewModel<T, CDTME, ADTME, LDT> model, final BlockingIndefiniteProgressLayer progressLayer, final AbstractEntityCentre<T, CDTME> owner) {
	super(model, progressLayer);
	this.owner = owner;
	this.loadAction = createLoadAction();
	this.exportAction = createExportAction();
	this.getModel().getPageHolder().addPageNavigationListener(createPageNavigationListener());
    }

    public AbstractEntityCentre<T, CDTME> getOwner() {
	return owner;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractAnalysisReviewModel<T, CDTME, ADTME, LDT> getModel() {
	return (AbstractAnalysisReviewModel<T, CDTME, ADTME, LDT>)super.getModel();
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
		//getProgressLayer().enableIncrementalLocking();
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

    /**
     * Creates the page navigation listener that enables or disable buttons according to the page navigation phase.
     * 
     * @return
     */
    private IPageNavigationListener createPageNavigationListener() {
	return new IPageNavigationListener() {

	    @Override
	    public void pageNavigated(final PageNavigationEvent event) {
		switch(event.getPageNavigationPhases()){
		case PRE_NAVIGATE:
		    enableRelatedActions(false, true);
		    break;
		case POST_NAVIGATE:
		case PAGE_NAVIGATION_EXCEPTION:
		    enableRelatedActions(true, true);
		}
	    }
	};
    }
}
