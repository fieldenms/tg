package ua.com.fielden.platform.swing.menu;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.tree.MutableTreeNode;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAnalysisListener;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.DynamicReportWrapper.CentreClosingEvent;
import ua.com.fielden.platform.swing.menu.DynamicReportWrapper.CentreClosingListener;
import ua.com.fielden.platform.swing.menu.api.IItemSelector;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationView;

/**
 * A menu item type for representing save-as menu items with custom configurations of the corresponding principle menu item.
 * This class is for implementing {@link MiWithConfigurationSupport} as part of the logic to construct save-as child menu items.
 *
 * @author TG Team
 *
 */
public final class MiSaveAsConfiguration<T extends AbstractEntity<?>> extends TreeMenuItem<DynamicReportWrapper<T>> implements IItemSelector {

    private static final long serialVersionUID = 1628351742425600699L;

    private final IAnalysisListener analysisListener;

    public MiSaveAsConfiguration(//
	    //Tree menu item related parameters
	    final MiWithConfigurationSupport<T> parentItem,
	    //Entity centre related parameters
	    final String name) {
	super(new DynamicReportWrapper<T>(//
		//Tree menu item related parameters
		name,//
		parentItem.getView().getInfo(),//
		parentItem.getView().getTreeMenu(),//
		name,//
		parentItem.getView().getMenuItemClass(),//
		parentItem.getView().getCentreBuilder()));

	this.analysisListener = createAnalysisListener(parentItem.getView().getTreeMenu());
	getView().getCentreConfigurationView().getModel().addPropertyChangeListener(createCentreModeChangeListener());
	getView().addCentreClosingListener(new CentreClosingListener() {

	    @Override
	    public void centreClosing(final CentreClosingEvent event) {
		synchronizeAnalysis(parentItem.getView().getTreeMenu());
	    }

	});
	addAnalysis();
    }

    /**
     * Creates the centre's mode change listener that adds or removes analysis listener to the centre manager.
     *
     * @return
     */
    private PropertyChangeListener createCentreModeChangeListener() {
	return new PropertyChangeListener() {

	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		if("mode".equals(evt.getPropertyName())){
		    if(ReportMode.REPORT.equals(evt.getNewValue())){
			getView().getEntityCentreManager().addAnalysisListener(analysisListener);
		    }else{
			getView().getEntityCentreManager().removeAnalysisListener(analysisListener);
		    }
		}
	    }
	};
    }

    /**
     * Creates the {@link IAnalysisListener} that listens the analysis add/remove actions and adds or remove tree menu item wrapper for analysis.
     *
     * @return
     */
    private IAnalysisListener createAnalysisListener(final TreeMenuWithTabs<?> treeMenu) {
	return new IAnalysisListener(){

	    @Override
	    public void propertyStateChanged(final Class<?> nothing, final String name, final Boolean hasBeenInitialised, final Boolean oldState) {
		if(hasBeenInitialised){
		    addChild(treeMenu, name);
		}else{
		    removeChild(treeMenu, name);
		}
	    }

	};
    }

    /**
     * Removes the analysis tree node for the specified analysis name.
     *
     * @param treeMenu
     * @param name
     */
    private void removeChild(final TreeMenuWithTabs<?> treeMenu, final String name){
	for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
	    if (name.equals(getChildAt(childIndex).toString())) {
		treeMenu.getModel().getOriginModel().removeNodeFromParent((MutableTreeNode) getChildAt(childIndex));
	    }
	}
	treeMenu.getModel().getOriginModel().reload(this);
    }

    /**
     * Adds new analysis tree node for the specified analysis name.
     *
     * @param treeMenu
     * @param name
     */
    private void addChild(final TreeMenuWithTabs<?> treeMenu, final String name){
	addItem(new TreeMenuItemWrapper<T>(name));
	treeMenu.getModel().getOriginModel().reload(this);
    }

    /**
     * Synchronizes this tree node with centre model.
     *
     * @param treeMenu
     */
    private void synchronizeAnalysis(final TreeMenuWithTabs<?> treeMenu) {
	final List<String> analysis = getView().getEntityCentreManager().analysisKeys();
	for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
	    if (!analysis.contains(getChildAt(childIndex).toString())) {
		treeMenu.getModel().getOriginModel().removeNodeFromParent((MutableTreeNode) getChildAt(childIndex));
	    }
	}
	addAnalysis();
	treeMenu.getModel().getOriginModel().reload(this);
    }

    /**
     * Adds the analysis tree menu item to the menu.
     *
     */
    private void addAnalysis() {
	for (final String analysisName : getView().getEntityCentreManager().analysisKeys()) {
	    if (!containAnalysis(analysisName)) {
		addItem(new TreeMenuItemWrapper<T>(analysisName));
	    }
	}
    }

    /**
     * Returns the value that indicates whether specified analysis name is between children of this tree node.
     *
     * @param analysisName
     * @return
     */
    private boolean containAnalysis(final String analysisName) {
	for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
	    if (getChildAt(childIndex).toString().equals(analysisName)) {
		return true;
	    }
	}
	return false;
    }


    @Override
    public void selectTreeMenuItem(final String name) {
	final CentreConfigurationView<T, ?> centre = getView().getCentreConfigurationView();
	if(centre.getModel().getMode() == ReportMode.REPORT){
	    centre.getPreviousView().selectAnalysis(GridConfigurationModel.gridAnalysisName);
	}
    }

}
