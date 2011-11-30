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
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorView;
import ua.com.fielden.platform.swing.utils.DummyBuilder;

public class EntityCentreWizard<T extends AbstractEntity> extends AbstractWizardView<T> {

    private static final long serialVersionUID = -1304048423695832696L;

    public EntityCentreWizard(final DomainTreeEditorModel<T> treeEditorModel, final BlockingIndefiniteProgressLayer progressLayer) {
	super(treeEditorModel, progressLayer);
    }

    @Override
    protected void initView() {
	setLayout(new MigLayout("fill, insets 0", "[fill, grow]", "[fill, grow][]"));

	//Create domain tree editor (i.e. domain property tree with expression editor).
	final DomainTreeEditorView<T> domainTreeEditorView = new DomainTreeEditorView<T>(getTreeEditorModel());

	//Creates action panel with build and cancel actions.
	final JPanel actionPanel = new JPanel(new MigLayout("fill, insets 10", "[][][]30:push[fill, :100:][fill, :100:]", "[c]"));
	actionPanel.add(DummyBuilder.label("Columns"));
	actionPanel.add(new JSpinner(createSpinnerModel()));
	actionPanel.add(createAutoRunCheckBox());
	actionPanel.add(new JButton(getBuildAction()));
	actionPanel.add(new JButton(getCancelAction()));

	add(domainTreeEditorView, "wrap");
	add(actionPanel);
    }

    private JCheckBox createAutoRunCheckBox() {
	//TODO implement item listener for the auto run check box.
	final JCheckBox autoRunCheckBox = new JCheckBox("Run automatically");
	//autoRunCheckBox.setSelected(getWizardModel().isAutoRun());
	autoRunCheckBox.addItemListener(new ItemListener() {

	    @Override
	    public void itemStateChanged(final ItemEvent e) {
		final int state = e.getStateChange();
		if (state == ItemEvent.SELECTED) {
		    //	    getWizardModel().setAutoRun(true);
		} else {
		    //	    getWizardModel().setAutoRun(false);
		}

	    }

	});
	return autoRunCheckBox;
    }

    private SpinnerModel createSpinnerModel() {
	final SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 4, 1);
	spinnerModel.addChangeListener(new ChangeListener() {

	    @Override
	    public void stateChanged(final ChangeEvent e) {
		//TODO implement this spinner model
	    }
	});
	return spinnerModel;
    }

}
