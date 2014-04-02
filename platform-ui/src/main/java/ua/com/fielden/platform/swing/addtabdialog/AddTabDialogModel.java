package ua.com.fielden.platform.swing.addtabdialog;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.swing.actions.Command;

public class AddTabDialogModel {

    private AddTabOptions returnValue;

    /**
     * Creates new
     * 
     * @param review
     */
    public AddTabDialogModel() {
        this.returnValue = AddTabOptions.CANCEL;
    }

    /**
     * Returns action that allows to abort "add tab sheet" action.
     * 
     * @param addTabDialog
     * @return
     */
    public Action getCancelAction(final AddTabDialog addTabDialog) {
        return new Command<Void>("Cancel") {
            private static final long serialVersionUID = -2730860592583696528L;

            {
                putValue(Action.SHORT_DESCRIPTION, "Cancels adding tab sheet");
                putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
            }

            @Override
            protected Void action(final ActionEvent e) throws Exception {
                return null;
            }

            @Override
            protected void postAction(final Void value) {
                super.postAction(value);
                returnValue = AddTabOptions.CANCEL;
                addTabDialog.closeDialog();
            }

        };
    }

    /**
     * Returns "save" action that allows to save current report configuration in to another file.
     * 
     * @param saveReportDialog
     * @return
     */
    public Action getApproveAction(final AddTabDialog addTabDialog) {
        return new Command<Void>("Ok") {

            private static final long serialVersionUID = -7758200445145077970L;

            {
                putValue(Action.SHORT_DESCRIPTION, "Creates new tab sheet");
                putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
            }

            @Override
            protected Void action(final ActionEvent e) throws Exception {
                return null;
            }

            @Override
            protected void postAction(final Void value) {
                super.postAction(value);
                if (StringUtils.isEmpty(addTabDialog.getEnteredTabName())) {
                    JOptionPane.showMessageDialog(addTabDialog, "Please choose valid name for new analysis report", "Information", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                returnValue = AddTabOptions.ADD_TAB;
                addTabDialog.closeDialog();
            }
        };
    }

    /**
     * Returns {@link AddTabOptions} instance that indicates chosen action
     * 
     * @return
     */
    public AddTabOptions getReturnValue() {
        return returnValue;
    }

}
