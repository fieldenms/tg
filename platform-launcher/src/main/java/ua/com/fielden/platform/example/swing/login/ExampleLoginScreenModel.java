/**
 *
 */
package ua.com.fielden.platform.example.swing.login;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JFrame;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.login.LoginScreenModel;

/**
 * Example model, demonstrating adding of login action to {@link LoginScreenModel}
 * 
 * @author Yura
 */
public class ExampleLoginScreenModel extends LoginScreenModel {

    private Action loginAction;

    public ExampleLoginScreenModel(final IAuthenticationModel userAuthenticator) {
        super(userAuthenticator);
    }

    void initLoginAction(final ExampleLoginScreen loginScreen) {
        if (loginAction != null) {
            throw new IllegalStateException("Cannot create login action twice");
        }
        loginAction = new BlockingLayerCommand<Result>("Login", loginScreen) {

            private static final long serialVersionUID = 1841855967814008406L;

            @Override
            protected Result action(final ActionEvent e) throws Exception {
                setMessage("Authenticating...");
                return ExampleLoginScreenModel.this.getAuthenticationResult();
            }

            @Override
            protected void postAction(final Result authenticationResult) {
                setMessage(authenticationResult.getMessage());
                loginScreen.getNotificationPanel().setMessage(authenticationResult.isSuccessful() ? authenticationResult.getMessage() : authenticationResult.getEx().getMessage(), authenticationResult.isSuccessful() ? MessageType.INFO
                        : MessageType.ERROR);
                super.postAction(authenticationResult);
            }
        };
    }

    public Action getLoginAction() {
        return loginAction;
    }

    @Override
    protected void authenticationPassed(final JFrame loginScreen, final Result authenticationResult) {
        System.out.println("user credentials are accepted");
    }

}
