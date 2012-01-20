package ua.com.fielden.platform.swing.dialogs;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.text.View;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * A dialog providing support for displaying detailed information. Useful for displaying exceptional situations where details may contain the stack trace.
 *
 * TODO Needs to be refined. This is a very crude implementation.
 *
 * @author 01es
 */
public class DialogWithDetails extends JDialog {
    private static final long serialVersionUID = 1L;

    /**
     * Possible dialog results.
     */
    public enum DialogResult {
	RESULT_CANCELLED, RESULT_AFFIRMED;
    }

    private DialogResult result;

    private final JPanel dialogPanel;
    private final JPanel contentPanel;
    private final JPanel buttonPanel;
    private final JPanel detailsPanel;
    private final Action closeAction;


    public DialogWithDetails(final Frame owner, final String title, final String msg, final String details, final Image appIcon) throws HeadlessException {
	super(owner, title, true);
	setIconImage(appIcon);
	closeAction = createCloseAction();

	// create all the panels including the main dialog panel
	detailsPanel = createDetailsPanel(details);
	detailsPanel.setVisible(false);
	contentPanel = contentPanel(msg);
	buttonPanel = buttonPanel();
	dialogPanel = createDialogPanel(contentPanel, buttonPanel);

	add(dialogPanel);

	pack();
	setLocationRelativeTo(owner);
    }

    public DialogWithDetails(final Frame owner, final String title, final String msg, final String details) throws HeadlessException {
	this(owner, title, msg, details, ResourceLoader.getImage("images/tg-icon.png"));
    }

    /**
     * A convenient constructor that builds dialog message and details content based on the exception.
     *
     * @param owner
     * @param title
     * @param msg
     * @param ex
     * @throws HeadlessException
     */
    public DialogWithDetails(final Frame owner, final String title, final Throwable ex) throws HeadlessException {
	this(owner, title, ex.getMessage(), composeDetails(ex));
    }

    public DialogWithDetails(final Frame owner, final String title, final Throwable ex, final Image appIcon) throws HeadlessException {
	this(owner, title, ex.getMessage(), composeDetails(ex), appIcon);
    }

    /**
     * Composes details content based on the provided exception.
     *
     * @param ex
     * @return
     */
    private static String composeDetails(final Throwable ex) {
	final StringBuilder builder = new StringBuilder();
	builder.append(ex.getClass().getName() + ": " + ex.getMessage() + "\n");
	for (final StackTraceElement stackTrace : ex.getStackTrace()) {
	    builder.append("\t" + stackTrace + "\n");
	}
	if (ex.getCause() != null) {
	    builder.append("Cause : \n" + composeDetails(ex.getCause()));
	}
	return builder.toString();
    }

    /**
     * Creates a close action. which is also a default action for ESC button.
     *
     * @return
     */
    protected Action createCloseAction() {
	return new AbstractAction("Close") {
	    private static final long serialVersionUID = 1L;

	    public void actionPerformed(final ActionEvent e) {
		setResult(DialogResult.RESULT_AFFIRMED);
		setVisible(false);
		dispose();
	    }
	};
    }

    /**
     * Creates the main dialog panel with initial layout.
     *
     * @param contentPanel
     * @param buttonPanel
     * @return
     */
    private JPanel createDialogPanel(final JPanel contentPanel, final JPanel buttonPanel) {
	final JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[200:400:, fill, grow]", "[fill, grow][]"));
	// assign ESC key stroke
	final String escActionName = "ESCAPE";
	final KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	final InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
	inputMap.put(escape, escActionName);
	panel.getActionMap().put(escActionName, closeAction);
	// add panels to the main dialog panel
	panel.add(contentPanel, "wrap");
	panel.add(buttonPanel, "wrap");
	return panel;
    }

    /**
     * Creates a panel used for displaying dialog details such as extended error message (e.g. exception stack trace).
     *
     * @param details
     * @return
     */
    protected JPanel createDetailsPanel(final String details) {
	final JTextArea textArea = new JTextArea(details);
	textArea.setEditable(false);
	textArea.setRows(10);

	final JLabel label = new JLabel("Details:");
	label.setLabelFor(textArea);

	final JPanel panel = new JPanel(new MigLayout("fill", "[200:400:, fill,grow]", "[][fill,grow]"));
	panel.add(label, "wrap");
	panel.add(new JScrollPane(textArea));
	return panel;
    }

    /**
     * Creates content panel consisting of a label displaying the main message.
     *
     * @param message
     * @return
     */
    protected JPanel contentPanel(final String message) {
	// TODO Needs to be enhanced to support icons as in ordinary error, warning and information dialogs.
	final JPanel panel = new JPanel(new MigLayout("fill", "[fill, grow, c]", "[:100:, fill, grow, c]"));

	// create the label displaying the main dialog message and determine its preferred size based on its view and predefined dialog width.
	final String msg = StringUtils.isEmpty(message) ? "There is no specific message." : message;
	final JLabel label = new JLabel((msg.startsWith("<html>") ? msg : "<html>" + msg + "</html>").replaceAll("\\n", "<br/>"));
	label.setHorizontalAlignment(SwingConstants.CENTER);

	final View view = (View) label.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
	view.setSize(400, 300);
	final float w = view.getPreferredSpan(View.X_AXIS);
	final float h = view.getPreferredSpan(View.Y_AXIS);
	final Dimension pref = new Dimension((int) Math.ceil(w), (int) Math.ceil(h));
	label.setPreferredSize(pref);

	panel.add(label);
	return panel;
    }

    /**
     * Creates a panel located just beneath the content panel providing the relevant dialog buttons (close and details).
     *
     * @return
     */
    protected JPanel buttonPanel() {
	final JPanel panel = new JPanel(new MigLayout("fill", "[]push[][]", "[c]"));

	final JButton closeButton = new JButton(closeAction);
	final JButton detailButton = new JButton(createDetailsAction(closeButton));

	panel.add(closeButton, "skip 1");
	panel.add(detailButton);

	getRootPane().setDefaultButton(closeButton);
	return panel;
    }

    protected Action createDetailsAction(final JButton closeButton) {
	final Action detailButtonAction = new AbstractAction("Details >>") {
	    private static final long serialVersionUID = 1L;

	    public void actionPerformed(final ActionEvent e) {
		if (detailsPanel.isVisible()) {
		    detailsPanel.setVisible(false);
		    putValue(Action.NAME, "Details <<");

		    dialogPanel.removeAll();
		    dialogPanel.setLayout(new MigLayout("fill, insets 0", "[200:400:, fill, grow]", "[fill, grow][]"));
		    dialogPanel.add(contentPanel, "wrap");
		    dialogPanel.add(buttonPanel);
		} else {
		    detailsPanel.setVisible(true);
		    putValue(Action.NAME, "<< Details");

		    dialogPanel.removeAll();
		    dialogPanel.setLayout(new MigLayout("fill, insets 0", "[200:400:, fill, grow]", "[][][fill, grow]"));
		    dialogPanel.add(contentPanel, "wrap");
		    dialogPanel.add(buttonPanel, "wrap");
		    dialogPanel.add(detailsPanel, "grow");
		}

		getRootPane().setDefaultButton(closeButton);
		pack();
	    }
	};
	detailButtonAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
	return detailButtonAction;
    }

    /**
     * Gets the result.
     *
     * @return the result.
     */
    public DialogResult getResult() {
	return result;
    }

    /**
     * Sets the dialog result.
     *
     * @param dialogResult
     *            the new dialog result.
     */
    public void setResult(final DialogResult dialogResult) {
	result = dialogResult;
    }

    protected Action getCloseAction() {
        return closeAction;
    }
}