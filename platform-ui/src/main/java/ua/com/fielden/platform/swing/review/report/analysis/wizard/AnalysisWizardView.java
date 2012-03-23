package ua.com.fielden.platform.swing.review.report.analysis.wizard;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;


public class AnalysisWizardView<T extends AbstractEntity, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractWizardView<T> {

    private final AbstractEntityCentre<T, CDTME> owner;

    public AnalysisWizardView(final AbstractEntityCentre<T, CDTME> owner, final DomainTreeEditorModel<T> treeEditorModel, final BlockingIndefiniteProgressLayer progressLayer) {
	super(treeEditorModel, "Choose distribution and aggregation properties", progressLayer);
	this.owner = owner;
	layoutComponents();
    }


}
