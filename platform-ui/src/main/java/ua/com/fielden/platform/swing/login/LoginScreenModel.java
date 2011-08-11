/**
 *
 */
package ua.com.fielden.platform.swing.login;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.swing.utils.DummyBuilder;

/**
 * Encapsulates basic components and logic for login screen. It may be subclasses to provide additional logic (for instance, action to be performed to actually log in)
 * 
 * @author TG Team
 */
public abstract class LoginScreenModel<VIEW extends JFrame> {

    private final JLabel usernameLabel;

    private final JTextField usernameField;

    private final JLabel passwordLabel;

    private final JPasswordField passwordField;

    private final IAuthenticationModel userAuthenticator;

    /**
     * Constructs instance of {@link LoginScreenModel} and creates all its components. Should be provided {@link IAuthenticationModel} instance for authentication
     */
    public LoginScreenModel(final IAuthenticationModel userAuthenticator) {
	this.userAuthenticator = userAuthenticator;

	this.usernameLabel = DummyBuilder.label("Username");
	this.usernameField = new JTextField("");

	this.passwordLabel = DummyBuilder.label("Password");
	this.passwordField = new JPasswordField();
	passwordField.setEchoChar('*');
    }

    /**
     * Returns editor for entering user name
     * 
     * @return
     */
    public JComponent getUsernameEditor() {
	return usernameField;
    }

    /**
     * Returns masked editor for entering password
     * 
     * @return
     */
    public JComponent getPasswordEditor() {
	return passwordField;
    }

    /**
     * Returns label to be placed before user name editor
     * 
     * @return
     */
    public JComponent getUsernameLabel() {
	return usernameLabel;
    }

    /**
     * Returns label to placed before password editor
     * 
     * @return
     */
    public JComponent getPasswordLabel() {
	return passwordLabel;
    }

    /**
     * Returns {@link Result} of authentication of username/password typed in respective fields
     * 
     * @see IAuthenticationModel#authenticate(String, String)
     * @return
     */
    public Result getAuthenticationResult() {
	return userAuthenticator.authenticate(getUsername(), getPassword());
    }

    /**
     * Returns user name, typed in user name editor
     * 
     * @return
     */
    private String getUsername() {
	return usernameField.getText();
    }

    /**
     * Returns password, typed in password editor
     * 
     * @return
     */
    private String getPassword() {
	return String.valueOf(passwordField.getPassword());
    }

    /**
     * This method is called in loginAction's postAction method (and thus on EDT) if authentication passed. Override this method in order to provide some functionality (hiding
     * login screen, showing application, etc.)
     * 
     * @param loginScreen
     *            -- a frame representing login screen
     * @param authenticationResult
     *            -- authentication result
     */
    protected abstract void authenticationPassed(final VIEW loginScreen, final Result authenticationResult);

}
