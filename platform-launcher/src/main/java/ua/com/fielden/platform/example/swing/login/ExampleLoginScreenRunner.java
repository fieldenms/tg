/**
 *
 */
package ua.com.fielden.platform.example.swing.login;

import java.awt.BorderLayout;
import java.util.Arrays;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.example.entities.Person;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

import com.jidesoft.plaf.LookAndFeelFactory;

/**
 * Login screen example runner.
 * 
 * @author Yura
 */
public class ExampleLoginScreenRunner {

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
                    return new Result(null, "Authentication failed", new Exception("Unknown user"));
                } else {
                    return new Result(null, "Authentication failed", new Exception("Incorrect password"));
                }
            }
        };

        SwingUtilitiesEx.invokeLater(new Runnable() {
            public void run() {
                final ExampleLoginScreenModel loginScreenModel = new ExampleLoginScreenModel(userAuthenticator);
                final ExampleLoginScreen loginScreen = new ExampleLoginScreen(loginScreenModel);
                SimpleLauncher.show("Login screen demo", new BorderLayout(), loginScreen);
                // acquiring focus in username editor
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        loginScreenModel.getUsernameEditor().requestFocusInWindow();
                    }
                });
            }
        });
    }

}
