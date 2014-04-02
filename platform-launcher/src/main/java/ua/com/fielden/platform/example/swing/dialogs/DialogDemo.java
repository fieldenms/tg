package ua.com.fielden.platform.example.swing.dialogs;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;

/**
 * Demonstrates dialog with details in two basic forms -- for exception and for a long message.
 * 
 * @author 01es
 * 
 */
public class DialogDemo {
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

        final Exception ex = new Exception("Demo exception");
        new DialogWithDetails(null, "Dialog Example with Exception", ex).setVisible(true);

        final String msg = "<html>this is a really long message: this is a really long message this is a really long message this is a really long message this is a really long message this is a really long message this is a really long message "
                + "this is a really long message this is a really long message this is a really long message this is a really long message this is a really long message this is a really long message "
                + "this is a really long message this is a really long message this is a really long message this is a really long message this is a really long message this is a really long message "
                + "this is a really long message this is a really long message this is a really long message this is a really long message this is a really long message this is a really long message "
                + "this is a really long message this is a really long message this is a really long message this is a really long message this is a really long message this is a really long message</html>";
        new DialogWithDetails(null, "Dialog Example with Long Mesasge", msg, "There is no additional information.").setVisible(true);
    }

}
