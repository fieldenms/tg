package ua.com.fielden.platform.example.swing.analysisreporttree;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ua.com.fielden.platform.example.entities.Vehicle;
import ua.com.fielden.platform.swing.dynamicreportstree.AnalysisTree;
import ua.com.fielden.platform.swing.dynamicreportstree.TreePanel;
import ua.com.fielden.platform.swing.review.DefaultDynamicCriteriaPropertyFilter;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.treemodel.AnalysisTreeModel;

import com.jidesoft.plaf.LookAndFeelFactory;

public class AnalysisReportTreeExample {

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
		final AnalysisTreeModel treeModel = new AnalysisTreeModel(Vehicle.class, new DefaultDynamicCriteriaPropertyFilter());
		final AnalysisTree analysisTree = new AnalysisTree(treeModel);
		final JFrame frame = new JFrame("Analysis tree");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(640, 480));
		frame.add(new TreePanel(analysisTree));
		frame.pack();
		frame.setVisible(true);
	    }
	});
    }
}
