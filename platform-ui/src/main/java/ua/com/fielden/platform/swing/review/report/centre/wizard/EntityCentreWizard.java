package ua.com.fielden.platform.swing.review.report.centre.wizard;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;
import ua.com.fielden.platform.swing.utils.DummyBuilder;

public class EntityCentreWizard<T extends AbstractEntity<?>> extends AbstractWizardView<T> {

    private static final long serialVersionUID = -1304048423695832696L;

    public EntityCentreWizard(final DomainTreeEditorModel<T> treeEditorModel, final BlockingIndefiniteProgressLayer progressLayer) {
	super(treeEditorModel, "Choose properties for selection criteria and result set", progressLayer);
	layoutComponents();
    }

    @Override
    public ICentreDomainTreeManager getDomainTreeManager() {
	return (ICentreDomainTreeManager)super.getDomainTreeManager();
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
