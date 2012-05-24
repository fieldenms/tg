package ua.com.fielden.platform.swing.review.report.analysis.view;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.pagination.model.development.IPageNavigationListener;
import ua.com.fielden.platform.swing.pagination.model.development.PageNavigationEvent;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReview;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;
import ua.com.fielden.platform.swing.review.report.events.LoadEvent;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;

public abstract class AbstractAnalysisReview<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer, ADTME extends IAbstractAnalysisDomainTreeManagerAndEnhancer, LDT> extends AbstractEntityReview<T, CDTME> {

    private static final long serialVersionUID = -1195915524813089236L;


    private final Action loadAction;
    private final Action exportAction;

    private boolean wasLoaded;

    public AbstractAnalysisReview(final AbstractAnalysisReviewModel<T, CDTME, ADTME, LDT> model, final AbstractAnalysisConfigurationView<T, CDTME, ADTME, LDT, ? extends AbstractAnalysisReview<T, CDTME, ADTME, LDT>> owner) {
	super(model, owner);
	this.loadAction = createLoadAction();
	this.exportAction = createExportAction();
	this.wasLoaded = false;
	this.getModel().getPageHolder().addPageNavigationListener(createPageNavigationListener());
	addComponentListener(createComponentWasResized());
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractAnalysisReviewModel<T, CDTME, ADTME, LDT> getModel() {
	return (AbstractAnalysisReviewModel<T, CDTME, ADTME, LDT>)super.getModel();
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractAnalysisConfigurationView<T, CDTME, ADTME, LDT, ? extends AbstractAnalysisReview<T, CDTME, ADTME, LDT>> getOwner() {
	return (AbstractAnalysisConfigurationView<T, CDTME, ADTME, LDT, ? extends AbstractAnalysisReview<T, CDTME, ADTME, LDT>>)super.getOwner();
    }

    public void loadData(){
	loadAction.actionPerformed(null);
    }

    public void exportData(){
	//exportAction.actionPerformed(null);
    }

    @Override
    protected ConfigureAction createConfigureAction() {
	return new ConfigureAction(getOwner()) {

	    private static final long serialVersionUID = 5194133338699647240L;

	    {
		putValue(Action.NAME, "Configure");
		putValue(Action.SHORT_DESCRIPTION, "Configure analysis");
		putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/configure.png"));
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		getOwner().getModel().freeze();
		return null;
	    }

	    @Override
	    protected void restoreAfterError() {
		if(getOwner().getModel().isFreeze()){
		    getOwner().getModel().discard();
		}
	    }
	};
    }

    /**
     * Enables or disables actions related to this analysis (run, export, paginator actions e.t.c.). The second parameter determines
     * whether this method was invoked after page navigation or after the data loading.
     *
     * @param enable
     * @param navigate
     */
    abstract protected void enableRelatedActions(final boolean enable, final boolean navigate);

    private Action createLoadAction() {
	return new BlockingLayerCommand<Pair<Result,LDT>>("Run", getOwner().getProgressLayer()) {
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
		enableRelatedActions(false, false);
		if (!result) {
		    return result;
		}
		if (getOwner().getOwner().getCriteriaPanel() != null) {
		    getOwner().getOwner().getCriteriaPanel().updateModel();
		}
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

    /**
     * Creates action that exports data in to external file.
     *
     * @return
     */
    private Action createExportAction() {
	return null;
//	return new BlockingLayerCommand<Result>("Export", getOwner().getProgressLayer()) {
//	    private static final long serialVersionUID = 1L;
//
//	    {
//		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
//		putValue(Action.SHORT_DESCRIPTION, "Execute query");
//		setEnabled(true);
//	    }
//
//	    @Override
//	    protected boolean preAction() {
//		//getProgressLayer().enableIncrementalLocking();
//		setMessage("Loading...");
//		final boolean result = super.preAction();
//		enableRelatedActions(false, false);
//		if (!result) {
//		    return result;
//		}
//		if (getOwner().getOwner().getCriteriaPanel() != null) {
//		    getOwner().getOwner().getCriteriaPanel().updateModel();
//		}
//		return true;
//	    }
//
//	    @Override
//	    protected Result action(final ActionEvent e) throws Exception {
//		final Result result = getModel().canLoadData();
//		if(result.isSuccessful()){
//		    return new Pair<Result, LDT>(result, getModel().executeAnalysisQuery());
//		}
//		return new Pair<Result, LDT>(result, null);
//	    }
//
//	    @Override
//	    protected void postAction(final Result result) {
//		if (!result.getKey().isSuccessful()) {
//		    JOptionPane.showMessageDialog(AbstractAnalysisReview.this, result.getKey().getMessage());
//		}
//		//		else {
//		//		    setDataToView(result.getValue()); // note that currently setting data to view and updating buttons state etc. perform in this single IReviewContract implementor method.
//		//		}
//		enableRelatedActions(true, false);
//		super.postAction(result);
//	    }
//
//	    /**
//	     * After default exception handling executed, post-actions should be performed to enable all necessary buttons, unlock layer etc.
//	     */
//	    @Override
//	    protected void handlePreAndPostActionException(final Throwable ex) {
//		super.handlePreAndPostActionException(ex);
//		enableRelatedActions(true, false);
//	    }
//	};
    }


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

    /**
     * Creates the {@link HierarchyListener} that determines when the component was shown and it's size was determined.
     *
     * @return
     */
    private ComponentListener createComponentWasResized() {
	return new ComponentAdapter() {

	    @Override
	    public void componentResized(final ComponentEvent e) {
		synchronized(AbstractAnalysisReview.this){
		    // should size change event be handled?
		    if (!wasLoaded) {
			// yes, so this one is first, lets handle it and set flag
			// to indicate that we won't handle any more
			// size changed events.
			wasLoaded = true;
			fireLoadEvent(new LoadEvent(AbstractAnalysisReview.this));

			// after this handler end its execution, lets remove it
			// from component because it is already not-useful
			final ComponentListener refToThis = this;
			SwingUtilitiesEx.invokeLater(new Runnable() {
			    public void run() {
				removeComponentListener(refToThis);
			    }
			});
		    }
		}
	    }
	};
    }
}
