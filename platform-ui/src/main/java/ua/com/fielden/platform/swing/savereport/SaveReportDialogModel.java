package ua.com.fielden.platform.swing.savereport;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListModel;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.ui.config.api.interaction.ICenterConfigurationController;

/**
 * Model for the {@link SaveReportDialog}.
 * 
 * @author oleh
 * 
 */
public class SaveReportDialogModel {

    private final String principleKey;

    private final ICenterConfigurationController centerController;

    private SaveReportOptions returnValue;

    /**
     * Creates new {@link SaveReportDialogModel} instance
     * 
     * @param directoryForReports
     *            - the directory where removable reports must be saved.
     */
    public SaveReportDialogModel(final String principleKey, final ICenterConfigurationController centerController) {
	this.principleKey = principleKey;
	this.centerController = centerController;
	returnValue = SaveReportOptions.CANCEL;
    }

    /**
     * Returns the {@link ListModel} with existing ad hoc reports.
     * 
     * @return
     */
    public ListModel getAvailableReports() {
	final DefaultListModel listModel = new DefaultListModel();
	for (final String centerName : centerController.getNonPrincipleCenters(principleKey)) {
	    listModel.addElement(centerName);
	}
	return listModel;
    }

    /**
     * Returns "save" action that allows to save current report configuration in to another file.
     * 
     * @param saveReportDialog
     * @return
     */
    public Action getApproveAction(final SaveReportDialog saveReportDialog) {
	return new Command<Void>("Save") {

	    private static final long serialVersionUID = -7758200445145077970L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "Creates the report");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
	    }

	    @Override
	    protected boolean preAction() {
		final boolean result = super.preAction();
		if (!result) {
		    return result;
		}
		final String reportTitle = saveReportDialog.getEnteredFileName().trim();
		if (StringUtils.isEmpty(reportTitle)) {
		    JOptionPane.showMessageDialog(saveReportDialog, "Please provide report title.", "Save Report Warning", JOptionPane.WARNING_MESSAGE);
		} else if (!saveReportDialog.isNameAvailable(reportTitle)) {
		    saveReportDialog.selectItem(reportTitle);
		    JOptionPane.showMessageDialog(saveReportDialog, "Report with this title already exists.", "Save Report Warning", JOptionPane.WARNING_MESSAGE);
		} else if (!centerController.isNonPrincipleCenterNameValid(principleKey, reportTitle)) {
		    JOptionPane.showMessageDialog(saveReportDialog, "The title contains illegal characters. Please change the title and try again.", "Save Report Warning", JOptionPane.WARNING_MESSAGE);
		} else {
		    return true;
		}
		return false;
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		returnValue = SaveReportOptions.APPROVE;
		saveReportDialog.closeDialog();
	    }
	};
    }

    /**
     * Returns action that allows to abort "save" action.
     * 
     * @param saveReportDialog
     * @return
     */
    public Action getCancelAction(final SaveReportDialog saveReportDialog) {
	return new Command<Void>("Cancel") {
	    private static final long serialVersionUID = -2730860592583696528L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "Cancel saving");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		returnValue = SaveReportOptions.CANCEL;
		saveReportDialog.closeDialog();
	    }

	};
    }

    /**
     * Returns {@link SaveReportOptions} instance that indicates chosen action
     * 
     * @return
     */
    public SaveReportOptions getReturnValue() {
	return returnValue;
    }

}
