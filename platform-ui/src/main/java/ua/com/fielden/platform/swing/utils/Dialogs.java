package ua.com.fielden.platform.swing.utils;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Provides a set of methods for displaying different dialogs available as part of {@link JOptionPane} in a EDT-safe way.
 *
 * @author TG Team
 *
 */
public enum Dialogs {

    ERROR_MESSAGE(JOptionPane.ERROR_MESSAGE), //
    INFORMATION_MESSAGE(JOptionPane.INFORMATION_MESSAGE), //
    WARNING_MESSAGE(JOptionPane.WARNING_MESSAGE), //
    QUESTION_MESSAGE(JOptionPane.QUESTION_MESSAGE), //
    PLAIN_MESSAGE(JOptionPane.PLAIN_MESSAGE);

    final int msgType;

    Dialogs(final int msgType) {
	this.msgType = msgType;
    }

    public static void showMessageDialog(final Component parentComponent, final String message, final String title, final Dialogs messageType) throws HeadlessException {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		JOptionPane.showMessageDialog(parentComponent, message, title, messageType.msgType);
	    }
	});
    }

    /**
     * A convenient method to displaying a dialog when no parent is available, but there is a need to provide a custom title bar icon.
     * The method works by creating a temporary frame with the provided icon set, which is then used as the dialog's parent.
     *
     * @param message
     * @param title
     * @param messageType
     * @throws HeadlessException
     */
    public static void showMessageDialog(final Image titleImage, final String message, final String title, final Dialogs messageType) throws HeadlessException {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		final JFrame frm = new JFrame();
		frm.setIconImage(titleImage);
		JOptionPane.showMessageDialog(frm, message, title, messageType.msgType);
		frm.dispose();
	    }
	});
    }


    public static int showYesNoCancelDialog(final Component parentComponent, final String message, final String title) {
	final int[] result = new int[] { -1 };
	try {
	    SwingUtilitiesEx.invokeAndWaitIfPossible(new Runnable() {
		@Override
		public void run() {
		    result[0] = JOptionPane.showConfirmDialog(parentComponent, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		}
	    });
	} catch (final Exception ex) {
	    throw new IllegalStateException(ex);
	}

	return result[0];
    }

    public static int showYesNoDialog(final Component parentComponent, final String message, final String title) {
	final int[] result = new int[] { -1 };
	try {
	    SwingUtilitiesEx.invokeAndWaitIfPossible(new Runnable() {
		@Override
		public void run() {
		    result[0] = JOptionPane.showConfirmDialog(parentComponent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		}
	    });
	} catch (final Exception ex) {
	    throw new IllegalStateException(ex);
	}

	return result[0];
    }

//    public static void main(final String[] args) throws Exception {
//        System.out.println(System.getProperty("os.name")
//            + " " + System.getProperty("os.version")
//            + " " + System.getProperty("java.version"));
//        final UIManager.LookAndFeelInfo[] lfa =
//            UIManager.getInstalledLookAndFeels();
//        for (final UIManager.LookAndFeelInfo lf : lfa) {
//            UIManager.setLookAndFeel(lf.getClassName());
//            final UIDefaults uid = UIManager.getLookAndFeelDefaults();
//            System.out.println("***"
//                + " " + lf.getName()
//                + " " + lf.getClassName()
//                + " " + uid.size() + " entries");
//
//            final Enumeration newKeys = uid.keys();
//
//            while (newKeys.hasMoreElements()) {
//              final Object obj = newKeys.nextElement();
//              System.out.printf("%50s : %s\n", obj, UIManager.get(obj));
//            }
//        }
//    }

}
