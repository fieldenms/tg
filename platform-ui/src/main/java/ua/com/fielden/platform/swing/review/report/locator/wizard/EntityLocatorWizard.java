package ua.com.fielden.platform.swing.review.report.locator.wizard;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.report.centre.configuration.LocatorConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.BuildAction;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.CancelAction;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;

public class EntityLocatorWizard<T extends AbstractEntity<?>, R extends AbstractEntity<?>> extends AbstractWizardView<T> {

    private static final long serialVersionUID = -5516220498620289020L;

    public EntityLocatorWizard(final LocatorConfigurationView<T, R> owner, final DomainTreeEditorModel<T> treeEditorModel) {
	super(owner, treeEditorModel,  "Choose properties for selection criteria and result set");
    }

    @SuppressWarnings("unchecked")
    @Override
    public LocatorConfigurationView<T, R> getOwner() {
	return (LocatorConfigurationView<T, R>)super.getOwner();
    }

    @Override
    protected BuildAction createBuildAction() {
	return new BuildAction(getOwner()) {

	    private static final long serialVersionUID = 2884294533491901193L;

	    {
		putValue(Action.NAME, "Build");
		putValue(Action.SHORT_DESCRIPTION, "Build this locator");
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		if(getOwner().getModel().isInFreezedPhase()){
		    getOwner().getModel().save();
		}
		return null;
	    }
	};
    }

    @Override
    protected CancelAction createCancelAction() {
	return new CancelAction(getOwner()) {

	    private static final long serialVersionUID = -6559513807527786195L;

	    {
		putValue(Action.NAME, "Cancel");
		putValue(Action.SHORT_DESCRIPTION, "Discard changes for this locator");
	    }

	    @Override
	    protected boolean preAction() {
		if(!super.preAction()){
		    return false;
		}
		if(!getOwner().getModel().isInFreezedPhase()){
		    JOptionPane.showMessageDialog(EntityLocatorWizard.this, "This locator's wizard can not be canceled!", "Warning", JOptionPane.WARNING_MESSAGE);
		    return false;
		}
		return true;
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		if(getOwner().getModel().isInFreezedPhase()){
		    getOwner().getModel().discard();
		}
		return null;
	    }
	};
    }

}
