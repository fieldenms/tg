/**
 *
 */
package ua.com.fielden.platform.example.swing.login;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.menu.MenuNotificationPanel;

public class ExampleLoginScreen extends BlockingIndefiniteProgressLayer {

    private static final long serialVersionUID = 6582759873170989961L;

    private final MenuNotificationPanel notificationPanel;

    public ExampleLoginScreen(final ExampleLoginScreenModel model) {
	super(new JPanel(new MigLayout("fill, insets 2", "[:80:][]", "[][][][]")), "");
	this.notificationPanel = new MenuNotificationPanel("Login screen");

	model.initLoginAction(this);

	final JPanel panel = (JPanel) getView();
	panel.add(notificationPanel, "span 2, grow, wrap");

	panel.add(model.getUsernameLabel(), "grow");
	panel.add(model.getUsernameEditor(), "grow, wrap");

	panel.add(model.getPasswordLabel(), "grow");
	panel.add(model.getPasswordEditor(), "grow, wrap");

	final JButton button = new JButton("Login");
	button.setAction(model.getLoginAction());
	panel.add(button, "grow, span 2");
    }

    protected MenuNotificationPanel getNotificationPanel() {
	return notificationPanel;
    }

}
