/**
 *
 */
package ua.com.fielden.platform.swing.login;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.swing.actions.BlockingCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;

/**
 * Model for {@link StyledLoginScreen} encapsulating title, background image and login and exit actions.
 * 
 * @author TG Team
 */
public abstract class StyledLoginScreenModel extends LoginScreenModel<StyledLoginScreen> {

    private Action loginAction;

    private Action exitAction;

    private final String title;

    private final ImageIcon backgroundImage;

    private StyledLoginScreen view;

    /**
     * Creates instance of {@link StyledLoginScreenModel} with {@link IAuthenticationModel}, title and background image set
     * 
     * @param userAuthenticator
     * @param title
     * @param backgroundImage
     */
    public StyledLoginScreenModel(final IAuthenticationModel userAuthenticator, final String title, final ImageIcon backgroundImage) {
        super(userAuthenticator);

        this.title = title;
        this.backgroundImage = backgroundImage;
    }

    public ImageIcon getBackgroundImage() {
        return backgroundImage;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Initialises login action.<br>
     * <br>
     * Note : can be initialised only once
     * 
     * @param loginScreen
     */
    private void initLoginAction(final StyledLoginScreen loginScreen) {
        if (loginAction != null) {
            throw new IllegalStateException("Can not initialise login action twice");
        }
        loginAction = new BlockingCommand<Result>("Log In", loginScreen.getBlockingPane()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean preAction() {
                super.preAction();

                setMessage("Authenticating...");
                return true;
            }

            @Override
            protected Result action(final ActionEvent e) throws Exception {
                return StyledLoginScreenModel.this.getAuthenticationResult();
            }

            @Override
            protected void postAction(final Result authResult) {
                setMessage(authResult.getMessage());

                loginScreen.getErrorLabel().setVisible(!authResult.isSuccessful());
                loginScreen.getErrorLabel().setText(!authResult.isSuccessful() ? authResult.getEx().getMessage() : "");
                loginScreen.getErrorLabel().setToolTipText(!authResult.isSuccessful() ? authResult.getMessage() : "");

                if (authResult.isSuccessful()) {
                    authenticationPassed(loginScreen, authResult);
                } else {
                    super.postAction(authResult);
                }
            }
        };
    }

    /**
     * Initialises exit action.<br>
     * <br>
     * Note : can be initialised only once
     * 
     * @param loginScreen
     */
    private void initExitAction(final StyledLoginScreen loginScreen) {
        if (exitAction != null) {
            throw new IllegalStateException("Can not initialise exit action twice");
        }
        exitAction = new AbstractAction("Exit") {
            private static final long serialVersionUID = -7025449323731054765L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                loginScreen.setVisible(false);
                loginScreen.dispose();
                // TODO close application more gracefully
                System.exit(0);
            }
        };
        exitAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
    }

    /**
     * Initialises login and exit actions. This method should be called only when {@link StyledLoginScreen#getBlockingPane()} is initialised.<br>
     * <br>
     * Note : can be called only once
     * 
     * @param loginScreen
     */
    void initActions(final StyledLoginScreen loginScreen) {
        view = loginScreen;
        initLoginAction(loginScreen);
        initExitAction(loginScreen);
    }

    public Action getLoginAction() {
        return loginAction;
    }

    public Action getExitAction() {
        return exitAction;
    }

    public BlockingIndefiniteProgressPane getBlockingPane() {
        return view.getBlockingPane();
    }

}
