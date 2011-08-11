/**
 *
 */
package ua.com.fielden.platform.swing.login;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;

/**
 * Login screen having image set as background.
 * 
 * @author TG Team
 */
public class StyledLoginScreen extends JFrame {
    private static final long serialVersionUID = 7357284184512270264L;

    private final BlockingIndefiniteProgressPane blockingPane;
    private final JLabel errorLabel;

    private final StyledLoginScreenModel loginScreenModel;

    /**
     * Creates {@link StyledLoginScreen}, which is non-resizable, sets its size to 800x630 and sets location to screen center. After all this, validates and packs itself
     * 
     * @param loginScreenModel
     */
    public StyledLoginScreen(final StyledLoginScreenModel loginScreenModel) {
	super(loginScreenModel.getTitle());
	setUndecorated(true);

	this.loginScreenModel = loginScreenModel;

	// order of following two calls is important
	blockingPane = new BlockingIndefiniteProgressPane(this);
	loginScreenModel.initActions(this);

	setLayout(new MigLayout("fill, insets 0"));
	final JPanel componentsPanel = createComponentsPanel(loginScreenModel.getBackgroundImage());
	errorLabel = createErrorLabel();

	componentsPanel.add(createSouthPanel(loginScreenModel), LoginScreenPart.SOUTH.getConstraints());
	componentsPanel.add(createLoginPanel(loginScreenModel, errorLabel), LoginScreenPart.EAST.getConstraints());

	getContentPane().add(componentsPanel, "grow");

	setPreferredSize(new Dimension(800, 630));
	setResizable(false);
	pack();

	RefineryUtilities.centerFrameOnScreen(this);
    }

    /**
     * Creates non-opaque components panel with imageIcon drawn on background. Any panels, that will be added to this panel should be non-opaque
     * 
     * @param imageIcon
     * @return
     */
    private JPanel createComponentsPanel(final ImageIcon imageIcon) {
	final JPanel componentsPanel = new JPanel(new MigLayout("fill")) {
	    private static final long serialVersionUID = 7424278167018647277L;

	    @Override
	    protected void paintComponent(final Graphics g) {
		// drawing image scaled to panel's size
		final Dimension size = getSize();
		g.drawImage(imageIcon.getImage(), 0, 0, (int) size.getWidth(), (int) size.getHeight(), null);

		super.paintComponent(g);
	    }
	};
	componentsPanel.setOpaque(false);
	componentsPanel.setPreferredSize(new Dimension(800, 600));
	return componentsPanel;
    }

    private JLabel createErrorLabel() {
	final JLabel errorLabel = new JLabel();
	errorLabel.setForeground(new Color(230, 0, 0));
	return errorLabel;
    }

    /**
     * Creates non-opaque south panel with exit button
     * 
     * @param model
     * @return
     */
    private JPanel createSouthPanel(final StyledLoginScreenModel model) {
	final JPanel southPanel = new JPanel(new MigLayout("fill", "20[:100:]push", "10[:25:]push"));
	southPanel.setOpaque(false);

	final JButton exitButton = new JButton(model.getExitAction());
	southPanel.add(exitButton, "grow");

	// assign ESC key stroke to call exit action
	final String ESC = "ESC";
	southPanel.getActionMap().put(ESC, model.getExitAction());
	southPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ESC);

	return southPanel;
    }

    /**
     * Creates non-opaque login panel
     * 
     * @param loginScreenModel
     * @param errorLabel
     * @return
     */
    private JPanel createLoginPanel(final StyledLoginScreenModel loginScreenModel, final JLabel errorLabel) {
	final JPanel loginPanel = new JPanel(new MigLayout("fill", "50[70::][120::]push", "200[25::][25::][25::][25::]push"));
	loginPanel.setOpaque(false);

	loginPanel.add(errorLabel, "skip, grow, wrap");
	loginPanel.add(loginScreenModel.getUsernameLabel(), "grow");
	loginPanel.add(loginScreenModel.getUsernameEditor(), "grow, wrap");
	loginPanel.add(loginScreenModel.getPasswordLabel(), "grow");
	loginPanel.add(loginScreenModel.getPasswordEditor(), "grow, wrap");

	final JButton loginButton = new JButton(loginScreenModel.getLoginAction());
	loginPanel.add(loginButton, "skip, grow");

	getRootPane().setDefaultButton(loginButton);
	return loginPanel;
    }

    public BlockingIndefiniteProgressPane getBlockingPane() {
	return blockingPane;
    }

    /**
     * Returns label for displaying error results
     * 
     * @return
     */
    public JLabel getErrorLabel() {
	return errorLabel;
    }

    @Override
    public void setVisible(final boolean b) {
	super.setVisible(b);
	getLoginScreenModel().getUsernameEditor().requestFocusInWindow();
    }

    /**
     * Enumeration containing login screen parts and mig-layout constraints for them
     * 
     * @author TG Team
     */
    public static enum LoginScreenPart {
	EAST {
	    @Override
	    public String getConstraints() {
		return "east, w 400!, h 450!";
	    }
	},
	WEST {
	    @Override
	    public String getConstraints() {
		return "west, w 400!, h 450!";
	    }
	},
	SOUTH {
	    @Override
	    public String getConstraints() {
		return "south, w 800!, h 75!";
	    }
	},
	NORTH {
	    @Override
	    public String getConstraints() {
		return "north, w 800!, h 75!";
	    }
	};

	public abstract String getConstraints();
    }

    public StyledLoginScreenModel getLoginScreenModel() {
	return loginScreenModel;
    }

}
