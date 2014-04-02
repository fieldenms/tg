/**
 *
 */
package ua.com.fielden.platform.example.swing.login;

import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.example.entities.Person;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.swing.login.StyledLoginScreen;
import ua.com.fielden.platform.swing.login.StyledLoginScreenModel;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

import com.jidesoft.plaf.LookAndFeelFactory;

/**
 * <br>
 * {@link StyledLoginScreen} usage example
 * 
 * @author TG Team
 */
public class StyledLoginScreenRunner {

    public static void main(final String[] args) {
        for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(laf.getName())) {
                try {
                    UIManager.setLookAndFeel(laf.getClassName());
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
        LookAndFeelFactory.installJideExtension();

        final IAuthenticationModel userAuthenticator = new IAuthenticationModel() {
            @Override
            public Result authenticate(final String username, final String password) {
                try {
                    Thread.sleep(1000);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                if ("YN".equals(username) && "password".equals(password)) {
                    return new Result(new Person("YN", ""), "Authentication passed");
                } else if ("OH".equals(username) && "password".equals(password)) {
                    return new Result(new Person("OH", ""), "Authentication passed");
                } else if ("NC".equals(username) && "password".equals(password)) {
                    return new Result(new Person("NC", ""), "Authentication passed");
                } else if ("JP".equals(username) && "password".equals(password)) {
                    return new Result(new Person("JP", ""), "Authentication passed");
                } else if ("OM".equals(username) && "password".equals(password)) {
                    return new Result(new Person("OM", ""), "Authentication passed");
                } else if (!Arrays.asList("YN", "OH", "NC", "JP", "OM").contains(username)) {
                    return new Result(null, "Authentication failed", new Exception("Incorrect user name or password"));
                } else {
                    return new Result(null, "Authentication failed", new Exception("Incorrect user name or password"));
                }
            }
        };

        SwingUtilitiesEx.invokeLater(new Runnable() {
            public void run() {
                final StyledLoginScreenModel loginScreenModel = new StyledLoginScreenModel(userAuthenticator, "Login Screen", new ImageIcon("src/main/resources/images/styled-login-scr-background.png")) {

                    @Override
                    protected void authenticationPassed(final StyledLoginScreen loginScreen, final Result authenticationResult) {
                        System.out.println("user credentials are accepted");
                        loginScreen.dispose();
                    }

                };
                final StyledLoginScreen loginScreen = new StyledLoginScreen(loginScreenModel);
                loginScreen.setVisible(true);
            }
        });
    }

}
