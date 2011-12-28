package ua.com.fielden.platform.swing.review.report.analysis.wizard;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;


public class AnalysisWizardView<T extends AbstractEntity> extends AbstractWizardView<T> {

    private final AbstractEntityCentre<T> owner;

    public AnalysisWizardView(final AbstractEntityCentre<T> owner, final DomainTreeEditorModel<T> treeEditorModel, final BlockingIndefiniteProgressLayer progressLayer) {
	super(treeEditorModel, progressLayer);
	this.owner = owner;
    }


}
