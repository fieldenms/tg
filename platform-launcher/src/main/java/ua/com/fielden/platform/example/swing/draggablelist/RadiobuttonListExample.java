package ua.com.fielden.platform.example.swing.draggablelist;

import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import ua.com.fielden.platform.swing.checkboxlist.CheckboxList;
import ua.com.fielden.platform.swing.checkboxlist.CheckboxListCellRenderer;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingModel.CheckingMode;
import ua.com.fielden.platform.swing.dnd.DnDSupport2;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

import com.jidesoft.plaf.LookAndFeelFactory;

public class RadiobuttonListExample {

    /**
     * @param args
     */
    public static void main(final String[] args) {
	SwingUtilitiesEx.installNimbusLnFifPossible();
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();

	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		final JFrame frame = new JFrame("Checkbox list example");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(200, 300));
		final DefaultListModel listModel = new DefaultListModel();
		listModel.add(0, "Red");
		listModel.add(1, "Green");
		listModel.add(2, "Blue");
		listModel.add(3, "White");
		final CheckboxList list = new CheckboxList(listModel);
		list.getCheckingModel().setCheckingMode(CheckingMode.SINGLE);
		list.setCellRenderer(new CheckboxListCellRenderer(new JRadioButton()));
		DnDSupport2.installDnDSupport(list, new DragFromSupportImplementation(list), new DragToSupportImplementation(list), true);
		frame.add(new JScrollPane(list));
		frame.pack();
		frame.setVisible(true);
	    }
	});
    }

}
