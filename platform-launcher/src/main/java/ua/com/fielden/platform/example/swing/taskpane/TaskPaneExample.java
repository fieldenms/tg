package ua.com.fielden.platform.example.swing.taskpane;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.taskpane.TaskPanel;

import com.jidesoft.plaf.LookAndFeelFactory;

/**
 * Example of the TaskPanel usage
 * 
 * @author oleh
 * 
 */
public class TaskPaneExample {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(laf.getName())) {
                        try {
                            UIManager.setLookAndFeel(laf.getClassName());
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
                LookAndFeelFactory.installJideExtension();
                final JFrame frame = new JFrame("task panel Example");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new MigLayout("fillx, insets 10"));
                final TaskPanel taskPane = new TaskPanel();
                taskPane.setLayout(new MigLayout("fill, insets 0"));
                taskPane.add(new JLabel("first label"), "growx, wrap");
                taskPane.add(new JLabel("second label"), "growx, wrap");
                taskPane.add(new JButton("first button"), "growx");
                taskPane.setTitle("Task pane");
                frame.add(taskPane, "growx");
                frame.setPreferredSize(new Dimension(640, 480));
                frame.pack();
                frame.setVisible(true);
            }

        });
    }
}
