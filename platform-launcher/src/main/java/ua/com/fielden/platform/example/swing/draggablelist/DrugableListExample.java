package ua.com.fielden.platform.example.swing.draggablelist;

import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import ua.com.fielden.platform.swing.dnd.DnDSupport2;

import com.jidesoft.plaf.LookAndFeelFactory;

public class DrugableListExample {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
        LookAndFeelFactory.installJideExtension();

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final JFrame frame = new JFrame("Dragable list example");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setPreferredSize(new Dimension(200, 300));
                final DefaultListModel<String> listModel = new DefaultListModel<String>();
                listModel.add(0, "Red");
                listModel.add(1, "Green");
                listModel.add(2, "Blue");
                listModel.add(3, "White");
                final JList<String> list = new JList<String>(listModel);
                DnDSupport2.installDnDSupport(list, new DragFromSupportImplementation(list), new DragToSupportImplementation(list), true);
                frame.add(new JScrollPane(list));
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

}
