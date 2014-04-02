package ua.com.fielden.platform.example.swing.command;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.swing.actions.Command;

/**
 * This is an example of a {@link Command} with exception.
 * 
 * @author 01es
 * 
 */
public class CommandsWithExceptionDemo extends AbstractUiApplication {

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Exception {
        final JFrame mainFrame = new JFrame("Action with exception");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new MigLayout("fill, insets 0", "[]", "[]"));
        mainFrame.add(new ButtonPanel(), "grow");
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

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

        new CommandsWithExceptionDemo().launch(args);
    }

}
