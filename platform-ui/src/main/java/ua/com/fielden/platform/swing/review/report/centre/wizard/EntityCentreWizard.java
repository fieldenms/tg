package ua.com.fielden.platform.swing.review.report.centre.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.BuildAction;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.CancelAction;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;
import ua.com.fielden.platform.swing.utils.DummyBuilder;

public class EntityCentreWizard<T extends AbstractEntity<?>, C extends AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer>> extends AbstractWizardView<T> {

    private static final long serialVersionUID = -1304048423695832696L;

    public EntityCentreWizard(final CentreConfigurationView<T, C> owner, final DomainTreeEditorModel<T> treeEditorModel) {
	super(owner, treeEditorModel, "Choose properties for selection criteria and result set");
	layoutComponents();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CentreConfigurationView<T, C> getOwner() {
	return (CentreConfigurationView<T, C>)super.getOwner();
    }

    @Override
    public ICentreDomainTreeManager getDomainTreeManager() {
	return (ICentreDomainTreeManager)super.getDomainTreeManager();
    }

    @Override
    protected BuildAction createBuildAction() {
	return new BuildAction(getOwner()) {

	    private static final long serialVersionUID = 2884294533491901193L;

	    {
		putValue(Action.NAME, "Build");
		putValue(Action.SHORT_DESCRIPTION, "Build this entity centre");
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		if(getOwner().getModel().isFreezed()){
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
		putValue(Action.SHORT_DESCRIPTION, "Discard changes for this entity centre");
	    }

	    @Override
	    protected boolean preAction() {
		if(!super.preAction()){
		    return false;
		}
		if(!getOwner().getModel().isFreezed()){
		    JOptionPane.showMessageDialog(EntityCentreWizard.this, "This locator's wizard can not be canceled!", "Warning", JOptionPane.WARNING_MESSAGE);
		    return false;
		}
		return true;
	    }

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		if(getOwner().getModel().isFreezed()){
		    getOwner().getModel().discard();
		}
		return null;
	    }
	};
    }

    @Override
    protected JPanel createActionPanel() {
	final JPanel actionPanel = new JPanel(new MigLayout("fill, insets 10", "[][][]30:push[fill, :100:][fill, :100:]", "[c]"));
	actionPanel.add(DummyBuilder.label("Columns"));
	actionPanel.add(new JSpinner(createSpinnerModel()));
	actionPanel.add(createAutoRunCheckBox());
	actionPanel.add(new JButton(getBuildAction()));
	actionPanel.add(new JButton(getCancelAction()));
	return actionPanel;
    }

    private JCheckBox createAutoRunCheckBox() {
	final ICentreDomainTreeManager centreManager = getDomainTreeManager();
	final JCheckBox autoRunCheckBox = new JCheckBox("Run automatically");
	autoRunCheckBox.setSelected(centreManager.isRunAutomatically());
	autoRunCheckBox.addItemListener(new ItemListener() {

	    @Override
	    public void itemStateChanged(final ItemEvent e) {
		final int state = e.getStateChange();
		if (state == ItemEvent.SELECTED) {
		    centreManager.setRunAutomatically(true);
		} else {
		    centreManager.setRunAutomatically(false);
		}

	    }

	});
	return autoRunCheckBox;
    }

    private SpinnerModel createSpinnerModel() {
	final IAddToCriteriaTickManager tickManager = getDomainTreeManager().getFirstTick();
	final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(tickManager.getColumnsNumber(), 1, 4, 1);
	spinnerModel.addChangeListener(new ChangeListener() {

	    @Override
	    public void stateChanged(final ChangeEvent e) {
		tickManager.setColumnsNumber(spinnerModel.getNumber().intValue());
	    }
	});
	return spinnerModel;
    }

}
