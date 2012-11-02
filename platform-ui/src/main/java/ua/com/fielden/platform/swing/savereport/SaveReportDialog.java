package ua.com.fielden.platform.swing.savereport;

import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * A dialog implementing user interaction for saving copies of ad hoc reports.
 *
 * @author TG Team
 *
 */
public class SaveReportDialog extends JPanel {

    private static final long serialVersionUID = -8579404164407875097L;

    private final JTextField newFileName = new JTextField();
    private final JList availableItems;

    private final JButton jbApprove;

    private final SaveReportDialogModel model;

    /**
     * Creates new {@link SaveReportDialog} for the specified {@link SaveReportDialogModel}.
     *
     * @param model
     */
    public SaveReportDialog(final SaveReportDialogModel model) {
	super(new MigLayout("fill, insets 3", "[grow,fill,:200:]", "[][][][grow,fill][]"));
	this.model = model;
	add(DummyBuilder.label("Report title"), "wrap");
	add(newFileName, "wrap");
	add(DummyBuilder.label("Saved reports"), "wrap");
	// TODO is that necessary to load all this stuff? model.getAvailableReports()
	availableItems = new JList();
	add(new JScrollPane(availableItems), "wrap");
	final JPanel buttonControl = new JPanel(new MigLayout("fill, insets 0", "push[:70:,fill][:70:,fill]", "[c,fill]"));
	buttonControl.add(jbApprove = new JButton(model.getApproveAction(this)));
	buttonControl.add(new JButton(model.getCancelAction(this)));
	add(buttonControl);
    }

    /**
     * Shows the {@link SaveReportDialog} and returns value that indicates the chosen action.
     *
     * @return
     */
    public SaveReportOptions showDialog() {
	availableItems.setModel(model.getAvailableReports());
	final JDialog availableDialog = new JDialog((Frame) null, "Save report", true);
	availableDialog.setIconImage(ResourceLoader.getImage("images/tg-icon.png"));
	availableDialog.setModalityType(ModalityType.APPLICATION_MODAL);
	availableDialog.add(this);

	final JRootPane rootPane = availableDialog.getRootPane();
	final InputMap iMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
	final ActionMap aMap = rootPane.getActionMap();
	aMap.put("escape", new AbstractAction() {
	    private static final long serialVersionUID = 1L;

	    public void actionPerformed(final ActionEvent e) {
		availableDialog.setVisible(false);
	    }
	});

	rootPane.setDefaultButton(jbApprove);

	availableDialog.pack();
	RefineryUtilities.centerFrameOnScreen(availableDialog);
	availableDialog.setVisible(true);
	availableDialog.removeAll();
	availableDialog.dispose();
	return model.getReturnValue();
    }

    /**
     * Returns title of the new ad hoc report.
     *
     * @return
     */
    public String getEnteredFileName() {
	return newFileName.getText();
    }

    /**
     * Selects specified item in the {@link JList} of existing reports.
     *
     * @param item
     */
    public void selectItem(final String item) {
	availableItems.setSelectedValue(item, true);
    }

    /**
     * Closes currently opened {@link SaveReportDialog}.
     */
    public void closeDialog() {
	if (SwingUtilities.getWindowAncestor(this) != null) {
	    SwingUtilities.getWindowAncestor(this).setVisible(false);
	}
    }

    /**
     * Determines whether specified name of the report can be used or not.
     *
     * @param name
     * @return
     */
    public boolean isNameAvailable(final String name) {
	return !((DefaultListModel) availableItems.getModel()).contains(name);
    }
}
