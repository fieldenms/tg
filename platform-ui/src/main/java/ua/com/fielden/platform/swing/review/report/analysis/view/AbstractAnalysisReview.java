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
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReview;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.utils.Pair;

public abstract class AbstractAnalysisReview<T extends AbstractEntity, ADTM extends IAbstractAnalysisDomainTreeManager, LDT> extends AbstractEntityReview<T, ICentreDomainTreeManager> {

    private static final long serialVersionUID = -1195915524813089236L;

    private final AbstractEntityCentre<T> owner;
    private final PageHolder pageHolder;

    private final Action loadAction;
    private final Action exportAction;

    public AbstractAnalysisReview(final AbstractAnalysisReviewModel<T, ADTM> model, final BlockingIndefiniteProgressLayer progressLayer, final AbstractEntityCentre<T> owner, final PageHolder pageHolder) {
	super(model, progressLayer);
	this.owner = owner;
	this.pageHolder = pageHolder;
	pageHolder.newPage(null);

	this.loadAction = createLoadAction();
	this.exportAction = createExportAction();
    }

    public AbstractEntityCentre<T> getOwner() {
	return owner;
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
		final Result result = canLoadData();
		if(result.isSuccessful()){
		    return new Pair<Result, LDT>(result, executeAnalysisQuery());
		}
		return new Pair<Result, LDT>(result, null);
	    }

	    @Override
	    protected void postAction(final Pair<Result,LDT> result) {
		if (!result.getKey().isSuccessful()) {
		    JOptionPane.showMessageDialog(AbstractAnalysisReview.this, result.getKey().getMessage());
		} else {
		    setDataToView(result.getValue()); // note that currently setting data to view and updating buttons state etc. perform in this single IReviewContract implementor method.
		}
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
     * Enables or disables actions related to this analysis (run, export, paginator actions e.t.c.). If the enable parameter is true then the second navigation parameter is ignored.
     * If the enable is false then the second parameter determines whether the loaded data is navigated or it is loading for the first time.
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

    /**
     * Determines whether this analysis can load data, and returns {@link Result} instance with exception, warning, or successful result.
     * 
     * @return
     */
    abstract protected Result canLoadData();

    /**
     * Executes analysis query, and returns the result set of the query execution.
     * 
     * @return
     */
    abstract protected LDT executeAnalysisQuery();

    /**
     * Must update view according to the passed data.
     * 
     * @param value
     */
    abstract protected void setDataToView(final LDT data);

    //    public PageHolder<AbstractEntity> getPageHolder(){
    //	return pageHolder;
    //    }

}
