package ua.com.fielden.platform.swing.addtabdialog;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * Dialog that allows one to choose title of the tab sheet with analysis.
 * 
 * @author oleh
 * 
 * @param <T>
 * @param <DAO>
 * @param <R>
 */
public class AddTabDialog extends JPanel {

    private static final long serialVersionUID = -5220975833916767366L;

    private final AddTabDialogModel model;
    private final JTextField newFileName = new JTextField();
    private final JButton jbApprove;

    /**
     * Creates new dialog that allows one to choose title of the new tab sheet
     * 
     * @param model
     */
    public AddTabDialog(final AddTabDialogModel model) {
        super(new MigLayout("fill, insets 3", "[grow,fill,:200:]", "[][][]"));
        this.model = model;
        add(DummyBuilder.label("Tab sheet title"), "wrap");
        add(newFileName, "wrap");
        final JPanel buttonControl = new JPanel(new MigLayout("fill, insets 0", "push[:70:,fill][:70:,fill]", "[c,fill]"));
        buttonControl.add(jbApprove = new JButton(model.getApproveAction(this)));
        buttonControl.add(new JButton(model.getCancelAction(this)));
        add(buttonControl);
    }

    /**
     * Closes this dialog.
     */
    public void closeDialog() {
        if (SwingUtilities.getWindowAncestor(this) != null) {
            SwingUtilities.getWindowAncestor(this).setVisible(false);
        }
    }

    /**
     * Shows the {@link AddTabDialog} and returns value that indicates the chosen action.
     * 
     * @return
     */
    public AddTabOptions showDialog(final Window window) {
        final JDialog availableDialog = new JDialog(window, "Add analysis report", ModalityType.APPLICATION_MODAL);
        availableDialog.setIconImage(ResourceLoader.getImage("images/tg-icon.png"));
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
     * Returns title of the tab sheet.
     * 
     * @return
     */
    public String getEnteredTabName() {
        return newFileName.getText();
    }
}
