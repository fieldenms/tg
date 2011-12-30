package ua.com.fielden.platform.swing.review.report.centre;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SingleSelectionModel;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationPanel;

import com.jidesoft.swing.JideTabbedPane;

public class MultipleAnalysisEntityCentre<T extends AbstractEntity> extends AbstractEntityCentre<T> {

    private static final long serialVersionUID = -5686015614708868918L;

    private final JideTabbedPane tabPanel;

    public MultipleAnalysisEntityCentre(final EntityCentreModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
	this.tabPanel = createReview();
	getReviewProgressLayer().setView(tabPanel);
	layoutComponents();
	//TODO Also must initiate other saved analysis.
    }

    @Override
    public JComponent getReviewPanel() {
	return getReviewProgressLayer();
    }

    private JideTabbedPane createReview() {
	final JideTabbedPane tabPane = new JideTabbedPane();
	tabPane.setModel(createTabbedPaneModel(tabPane));
	tabPane.setHideOneTab(true); // no need to show tab if there is only one
	tabPane.setShowCloseButton(true);
	tabPane.setShowCloseButtonOnTab(true);
	tabPane.setShowCloseButtonOnSelectedTab(true);
	tabPane.setColorTheme(JideTabbedPane.COLOR_THEME_OFFICE2003);
	tabPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
	tabPane.setBorder(BorderFactory.createLineBorder(new Color(146, 151, 161)));
	//Initiates first tab with main details (i.e. grid analysis).
	final GridConfigurationPanel<T> mainDetails = createGridAnalysis();
	tabPane.addTab(mainDetails.getAnalysisName(), mainDetails);

	tabPane.setTabClosableAt(0, false);
	tabPane.setSelectedIndex(0);
	tabPane.setCloseAction(createCloseTabAction(tabPane));
	return tabPane;
    }

    private Action createCloseTabAction(final JideTabbedPane tabPane) {
	return new AbstractAction() {

	    private static final long serialVersionUID = 6001475679820547729L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		//TODO implement close tab sheet.
	    }
	};
    }

    private SingleSelectionModel createTabbedPaneModel(final JideTabbedPane tabPane) {
	return new DefaultSingleSelectionModel() {

	    private static final long serialVersionUID = -3273219271999879724L;

	    @SuppressWarnings({ "unchecked", "rawtypes" })
	    @Override
	    public void setSelectedIndex(final int index) {
		if(!getReviewProgressLayer().isLocked()){
		    super.setSelectedIndex(index);
		    final AbstractAnalysisConfigurationView<T, ?, ?, ?, ?> analysis = (AbstractAnalysisConfigurationView)tabPane.getSelectedComponent();
		    setCurrentAnalysisConfigurationView(analysis);
		}else{
		    JOptionPane.showMessageDialog(tabPane, "The " + tabPane.getTitleAt(index) + " analysis can not be selected right now, " +
			    "because there is another action in progress!", "Information", JOptionPane.INFORMATION_MESSAGE);
		}
	    }
	};
    }


    private GridConfigurationPanel<T> createGridAnalysis(){
	final GridConfigurationModel<T> configModel = new GridConfigurationModel<T>(getModel().getCriteria());
	//TODO must consider what progress layer should be used.
	final GridConfigurationPanel<T> gridConfigView = new GridConfigurationPanel<T>("Main details", configModel, this, getReviewProgressLayer());
	gridConfigView.open();
	return gridConfigView;
    }
}
