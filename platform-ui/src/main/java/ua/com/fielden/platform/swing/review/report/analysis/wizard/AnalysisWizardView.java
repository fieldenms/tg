package ua.com.fielden.platform.swing.review.report.analysis.wizard;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.BuildAction;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.CancelAction;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTree2;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTreeModel2;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTreePanel;


public class AnalysisWizardView<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractWizardView<T> {

    private static final long serialVersionUID = 1664306691939896482L;

    private final EntitiesTreePanel<IAbstractAnalysisDomainTreeManager> entitiesTreePanel;

    public AnalysisWizardView(final AbstractAnalysisConfigurationView<T, CDTME, ?, ?> configurationOwner, final IAbstractAnalysisDomainTreeManager domainTreeManager) {
	super(configurationOwner, domainTreeManager, "Choose distribution and aggregation properties");
	this.addSelectionEventListener(createWizardSelectionListener());

	// Configuring the entities tree.
	final EntitiesTreeModel2<IAbstractAnalysisDomainTreeManager> treeModel = new EntitiesTreeModel2<IAbstractAnalysisDomainTreeManager>(domainTreeManager, "distribution properties", "aggregation properties");
	final EntitiesTree2<IAbstractAnalysisDomainTreeManager> tree = new EntitiesTree2<IAbstractAnalysisDomainTreeManager>(treeModel);
	entitiesTreePanel = new EntitiesTreePanel<IAbstractAnalysisDomainTreeManager>(tree);

	layoutComponents();
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractAnalysisConfigurationView<T, CDTME, ?, ?> getOwner() {
	return (AbstractAnalysisConfigurationView<T, CDTME, ?, ?>)super.getOwner();
    }

    @Override
    public JPanel getTreeView() {
        return entitiesTreePanel;
    }

    @Override
    protected BuildAction createBuildAction() {
	return new BuildAction(getOwner()) {

	    private static final long serialVersionUID = -4327459785830127546L;

	    {
		putValue(Action.NAME, "Build");
		putValue(Action.SHORT_DESCRIPTION, "Build this analysis");
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		if(getOwner().getModel().isFreeze()){
		    getOwner().getModel().save();
		}
		return super.action(e);
	    }

	    @Override
	    protected void restoreAfterError() {
		if(!getOwner().getModel().isFreeze()){
		    getOwner().getModel().freeze();
		}
	    }
	};
    }

    @Override
    protected CancelAction createCancelAction() {
	return new CancelAction(getOwner()) {

	    private static final long serialVersionUID = -2773251150992803625L;

	    {
		putValue(Action.NAME, "Cancel");
		putValue(Action.SHORT_DESCRIPTION, "Discard changes for this analysis");
	    }

	    @Override
	    protected boolean preAction() {
		if(!getOwner().getModel().isFreeze()){
		    JOptionPane.showMessageDialog(AnalysisWizardView.this, "This analysis wizard can not be canceled!", "Warning", JOptionPane.WARNING_MESSAGE);
		    return false;
		}
		if(!super.preAction()){
		    return false;
		}
		return true;
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		if(getOwner().getModel().isFreeze()){
		    getOwner().getModel().discard();
		}
		return super.action(e);
	    }

	    @Override
	    protected void restoreAfterError() {
		if(!getOwner().getModel().isFreeze()){
		    getOwner().getModel().freeze();
		}
	    }
	};
    }


    private ISelectionEventListener createWizardSelectionListener() {
	return new ISelectionEventListener() {

	    @Override
	    public void viewWasSelected(final SelectionEvent event) {
		getCentre().getPageHolderManager().selectPageHolder(null);
		//Managing the default, design and custom action changer button enablements.
		getCentre().getDefaultAction().setEnabled(getCentre().getCriteriaPanel() != null);
		if (getCentre().getCriteriaPanel() != null && getCentre().getCriteriaPanel().canConfigure()) {
		    getCentre().getCriteriaPanel().getSwitchAction().setEnabled(true);
		}
		if (getCentre().getCustomActionChanger() != null) {
		    getCentre().getCustomActionChanger().setEnabled(true);
		}
		//Managing the paginator's enablements.
		getCentre().getPaginator().setEnableActions(false, false);
		//Managing load and export enablements.
		getCentre().getExportAction().setEnabled(false);
		getCentre().getRunAction().setEnabled(false);

	    }
	};
    }

    /**
     * Returns the {@link AbstractEntityCentre} that owns this analysis wizard view.
     *
     * @return
     */
    private AbstractEntityCentre<T, CDTME> getCentre() {
	return getOwner().getOwner();
    }

}
