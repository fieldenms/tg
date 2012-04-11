package ua.com.fielden.platform.swing.review.report.analysis.wizard;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;


public class AnalysisWizardView<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractWizardView<T> {

    private static final long serialVersionUID = 1664306691939896482L;

    private final AbstractEntityCentre<T, CDTME> owner;

    public AnalysisWizardView(final AbstractEntityCentre<T, CDTME> owner, final DomainTreeEditorModel<T> treeEditorModel, final BlockingIndefiniteProgressLayer progressLayer) {
	super(treeEditorModel, "Choose distribution and aggregation properties", progressLayer);
	this.owner = owner;
	this.addSelectionEventListener(createWizardSelectionListener());
	layoutComponents();
    }

    private ISelectionEventListener createWizardSelectionListener() {
	return new ISelectionEventListener() {

	    @Override
	    public void viewWasSelected(final SelectionEvent event) {
		//Managing the default, design and custom action changer button enablements.
		getOwner().getDefaultAction().setEnabled(getOwner().getModel().getCriteria().isDefaultEnabled());
		if(getOwner().getCriteriaPanel() != null && getOwner().getCriteriaPanel().canConfigure()){
		    getOwner().getCriteriaPanel().getSwitchAction().setEnabled(true);
		}
		if(getOwner().getCustomActionChanger() != null){
		    getOwner().getCustomActionChanger().setEnabled(true);
		}
		//Managing the paginator's enablements.
		getOwner().getPaginator().setEnableActions(false, false);
		//Managing load and export enablements.
		getOwner().getExportAction().setEnabled(false);
		getOwner().getRunAction().setEnabled(false);

	    }
	};
    }

    /**
     * Returns the {@link AbstractEntityCentre} that owns this analysis wizard view.
     * 
     * @return
     */
    private AbstractEntityCentre<T, CDTME> getOwner() {
	return owner;
    }
}
