package ua.com.fielden.platform.swing.review.report.analysis.view;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPageNavigationListener;
import ua.com.fielden.platform.pagination.PageNavigationEvent;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.file.ExtensionFileFilter;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReview;
import ua.com.fielden.platform.swing.review.development.DefaultLoadingNode;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;
import ua.com.fielden.platform.utils.ResourceLoader;

public abstract class AbstractAnalysisReview<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer, ADTME extends IAbstractAnalysisDomainTreeManager> extends AbstractEntityReview<T, CDTME> {

    private static final long serialVersionUID = -1195915524813089236L;

    private final Action loadAction;
    private final Action exportAction;

    private final DefaultLoadingNode resizingLoadingNode;

    public AbstractAnalysisReview(final AbstractAnalysisReviewModel<T, CDTME, ADTME> model, final AbstractAnalysisConfigurationView<T, CDTME, ADTME, ?> owner) {
	super(model, owner);
	this.loadAction = createLoadAction();
	this.exportAction = createExportAction();
	this.resizingLoadingNode = new DefaultLoadingNode();
	this.getModel().setAnalysisView(this);
	this.getModel().getPageHolder().addPageNavigationListener(createPageNavigationListener());
	owner.getOwner().getPageHolderManager().addPageHolder(getModel().getPageHolder());
	addComponentListener(createComponentWasResized());
	addLoadingChild(resizingLoadingNode);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractAnalysisReviewModel<T, CDTME, ADTME> getModel() {
	return (AbstractAnalysisReviewModel<T, CDTME, ADTME>) super.getModel();
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractAnalysisConfigurationView<T, CDTME, ADTME, ? extends AbstractAnalysisReview<T, CDTME, ADTME>> getOwner() {
	return (AbstractAnalysisConfigurationView<T, CDTME, ADTME, ? extends AbstractAnalysisReview<T, CDTME, ADTME>>) super.getOwner();
    }

    @Override
    public void select() {
	getOwner().getOwner().getPageHolderManager().selectPageHolder(getModel().getPageHolder());
	super.select();
    }

    @Override
    public void close() {
	resizingLoadingNode.reset();
	setSize(new Dimension(0, 0));
	super.close();
    }

    public void loadData() {
	loadAction.actionPerformed(null);
    }

    public void exportData() {
	exportAction.actionPerformed(null);
    }

    /**
     * Flushes all the components' value of the selection criteria panel
     */
    public void commitComponents(){
	if (getOwner().getOwner().getCriteriaPanel() != null) {
	    getOwner().getOwner().getCriteriaPanel().updateModel();
	}
    }

    public IEntityDao<T> companionObject() {
	return getModel().getCriteria().companionObject();
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
	    protected boolean preAction() {
	        final boolean superRes = super.preAction();
	        if(!superRes){
	            return false;
	        }
	        getOwner().getOwner().getPageHolderManager().removePageHolder(getModel().getPageHolder());
	        return true;
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		getOwner().getModel().freeze();
		return super.action(e);
	    }

	    @Override
	    protected void restoreAfterError() {
		if (getOwner().getModel().isFreeze()) {
		    getOwner().getModel().discard();
		}
	    }
	};
    }

    /**
     * Enables or disables actions related to this analysis (run, export, paginator actions e.t.c.). The second parameter determines whether this method was invoked after page
     * navigation or after the data loading.
     *
     * @param enable
     * @param navigate
     */
    protected void enableRelatedActions(final boolean enable, final boolean navigate){
	getCentre().getActionPanel().setEnabled(enable);
	if (getCentre().getCriteriaPanel() != null) {
	    getCentre().getDefaultAction().setEnabled(enable);
	}
	getCentre().getRunAction().setEnabled(enable);
	if (getCentre().getCustomActionChanger() != null) {
	    getCentre().getCustomActionChanger().setEnabled(enable);
	}
	if (getCentre().getCriteriaPanel() != null && getCentre().getCriteriaPanel().canConfigure()) {
	    getCentre().getCriteriaPanel().getSwitchAction().setEnabled(enable);
	}
    }

    /**
     * Returns the {@link AbstractEntityCentre} that owns this analysis.
     *
     * @return
     */
    protected AbstractEntityCentre<T, CDTME> getCentre() {
	return getOwner().getOwner();
    }

    private Action createLoadAction() {
	return new BlockingLayerCommand<Result>("Run", getOwner().getProgressLayer()) {
	    private static final long serialVersionUID = 1L;

	    {
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
		putValue(Action.SHORT_DESCRIPTION, "Execute query");
		setEnabled(true);
	    }

	    @Override
	    protected boolean preAction() {
		getOwner().getProgressLayer().enableIncrementalLocking();
		setMessage("Loading...");
		final boolean result = super.preAction();
		if (!result) {
		    return result;
		}
		commitComponents();
		enableRelatedActions(false, false);
		return true;
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		return getModel().executeAnalysisQuery();
	    }

	    @Override
	    protected void postAction(final Result result) {
		if (!result.isSuccessful()) {
		    JOptionPane.showMessageDialog(AbstractAnalysisReview.this, result.getMessage());
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
	    }
	};

    }

    /**
     * Creates action that exports data in to external file.
     *
     * @return
     */
    private Action createExportAction() {
	return new BlockingLayerCommand<Result>("Export", getOwner().getProgressLayer()) {
	    private static final long serialVersionUID = 1L;

	    private String targetFileName;

	    {
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		putValue(Action.SHORT_DESCRIPTION, "Export data");
		setEnabled(true);
	    }

	    @Override
	    protected boolean preAction() {
		setMessage("Exporting...");
		getOwner().getProgressLayer().enableIncrementalLocking();
		final boolean result = super.preAction();
		if (!result) {
		    return result;
		}
		commitComponents();

		// let user choose a file for export
		final JFileChooser fileChooser = targetFileName == null ? new JFileChooser()
			: new JFileChooser(targetFileName.substring(0, targetFileName.lastIndexOf(File.separator)));
		final ExtensionFileFilter filter = new ExtensionFileFilter("Export files", getModel().getExportFileExtensions());
		fileChooser.addChoosableFileFilter(filter);

		boolean fileChosen = false;
		// prompt for a file name until the provided file has a correct extension or the save file dialog is cancelled.
		while (!fileChosen) {
		    // Determine which button was clicked to close the dialog
		    switch (fileChooser.showSaveDialog(AbstractAnalysisReview.this)) {
		    case JFileChooser.APPROVE_OPTION: // nothing else is relevant
			final File file = fileChooser.getSelectedFile();
			final String ext = ExtensionFileFilter.getExtension(file);
			if (StringUtils.isEmpty(ext)) {
			    targetFileName = file.getAbsolutePath() + "." + getModel().getDefaultExportFileExtension();
			    fileChosen = true;
			} else if (filter.accept(file)) {
			    targetFileName = file.getAbsolutePath();
			    fileChosen = true;
			}
			break;
		    case JFileChooser.CANCEL_OPTION: // Cancel or the close-dialog icon was clicked
			return false;
		    case JFileChooser.ERROR_OPTION: // The selection process did not complete successfully thus promt again
			fileChosen = false;
			break;
		    }

		    // check if file already exists and request override permission
		    if (fileChosen) {
			final File file = new File(targetFileName);
			if (file.exists()) {
			    fileChosen = JOptionPane.showConfirmDialog(AbstractAnalysisReview.this, "The file already exists. Overwrite?", "Export", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
			}
		    }
		}
		enableRelatedActions(false, false);
		return true;
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		return getModel().exportData(targetFileName);
	    }

	    @Override
	    protected void postAction(final Result result) {
		if (!result.isSuccessful()) {
		    JOptionPane.showMessageDialog(AbstractAnalysisReview.this, result.getMessage());
		} else {
		    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(AbstractAnalysisReview.this, "Saved successfully. Open?", "Export", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE)) {
			try {
			    Desktop.getDesktop().open(new File(targetFileName));
			} catch (final IOException e) {
			    JOptionPane.showMessageDialog(AbstractAnalysisReview.this, "Could not open file. Try opening using standard facilities.\n\n" + e.getMessage(), "Export", JOptionPane.WARNING_MESSAGE);
			}
		    }
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
	    }
	};
    }

    /**
     * Creates the page navigation listener that enables or disable buttons according to the page navigation phase.
     *
     * @return
     */
    private IPageNavigationListener createPageNavigationListener() {
	return new IPageNavigationListener() {

	    @Override
	    public boolean pageNavigated(final PageNavigationEvent event) {
		switch (event.getPageNavigationPhases()) {
		case PRE_NAVIGATE:
		    enableRelatedActions(false, true);
		    break;
		case POST_NAVIGATE:
		case PAGE_NAVIGATION_EXCEPTION:
		    enableRelatedActions(true, true);
		case NAVIGATE:
		    break;
		default:
		    break;
		}
		return true;
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
		synchronized (AbstractAnalysisReview.this) {
		    resizingLoadingNode.tryLoading();
		}
	    }
	};
    }
}
