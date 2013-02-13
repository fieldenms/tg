package ua.com.fielden.platform.example.swing.draggablelist;

import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxList;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxListCellRenderer;
import ua.com.fielden.platform.swing.dnd.DnDSupport2;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

import com.jidesoft.plaf.LookAndFeelFactory;

public class DoubleCheckboxesWithoutSortingExample {

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
		final DefaultListModel<String> listModel = new DefaultListModel<String>();
		listModel.add(0, "Red");
		listModel.add(1, "Green");
		listModel.add(2, "Blue");
		listModel.add(3, "White");
		final SortingCheckboxList<String> list = new SortingCheckboxList<String>(listModel, 2);
		list.setCellRenderer(new SortingCheckboxListCellRenderer<String>(list){

		    private static final long serialVersionUID = 7083287819656588503L;

		    @Override
		    public boolean isSortingAvailable(final String element) {
			return false;
		    }

		});
		list.setSortingModel(null);
		DnDSupport2.installDnDSupport(list, new DragFromSupportImplementation(list), new DragToSupportImplementation(list), true);
		frame.add(new JScrollPane(list));
		frame.pack();
		frame.setVisible(true);
	    }
	});
    }

}
