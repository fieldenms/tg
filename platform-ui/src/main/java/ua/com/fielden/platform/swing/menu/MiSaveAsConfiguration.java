package ua.com.fielden.platform.swing.menu;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.api.IItemSelector;

/**
 * A menu item type for representing save-as menu items with custom configurations of the corresponding principle menu item.
 * This class is for implementing {@link MiWithConfigurationSupport} as part of the logic to construct save-as child menu items.
 *
 * @author TG Team
 *
 */
public final class MiSaveAsConfiguration<T extends AbstractEntity> extends TreeMenuItem<DynamicReportWrapper<T>> implements IItemSelector {

    private static final long serialVersionUID = 1628351742425600699L;

    public MiSaveAsConfiguration(//
	    //Tree menu item related parameters
	    final MiWithConfigurationSupport<T> parentItem,
	    //Entity centre related parameters
	    final String name) {
	super(new DynamicReportWrapper<T>(//
		//Tree menu item related parameters
		name,//
		parentItem.getView().getInfo(),//
		parentItem.getTreeMenu(),//
		parentItem.getEntityType(),//
		name,//
		parentItem.getGlobalDomainTreeManager(),//
		parentItem.getEntityFactory(),//
		parentItem.getMasterManager(),//
		parentItem.getCriteriaGenerator()));
	//	getView().addCentreClosingListener(new CentreClosingListener() {
	//
	//	    @Override
	//	    public void centreClosing(final CentreClosingEvent event) {
	//		synchronizeAnalysis(treeMenu, criteriaBuilder);
	//	    }
	//
	//	});
	//	this.criteriaBuilder = criteriaBuilder;
	//	addAnalysis(criteriaBuilder);
    }

    //    public DynamicCriteriaModelBuilder<T, DAO, R> getDynamicCriteriaModelBuilder() {
    //	return criteriaBuilder;
    //    }
    //
    //    private void synchronizeAnalysis(final TreeMenuWithTabs<?> treeMenu, final DynamicCriteriaModelBuilder<T, DAO, R> criteriaBuilder) {
    //	final Set<String> analysis = criteriaBuilder.getWizardModel().getAnalysis().keySet();
    //	for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
    //	    if (!analysis.contains(getChildAt(childIndex).toString())) {
    //		treeMenu.getModel().getOriginModel().removeNodeFromParent((MutableTreeNode) getChildAt(childIndex));
    //	    }
    //	}
    //	addAnalysis(criteriaBuilder);
    //	treeMenu.getModel().getOriginModel().reload(this);
    //    }
    //
    //    private void addAnalysis(final DynamicCriteriaModelBuilder<T, DAO, R> criteriaBuilder){
    //	for (final String analysisName : criteriaBuilder.getWizardModel().getAnalysis().keySet()) {
    //	    if(!containAnalysis(analysisName)){
    //		addItem(new TreeMenuItemWrapper<T, DAO, R>(analysisName, this.isGroupItem()));
    //	    }
    //	}
    //    }
    //
    //    /**
    //     * Returns the value that indicates whether specified analysis name is between children of this tree node.
    //     *
    //     * @param analysisName
    //     * @return
    //     */
    //    private boolean containAnalysis(final String analysisName){
    //	for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
    //	    if (getChildAt(childIndex).toString().equals(analysisName)) {
    //		return true;
    //	    }
    //	}
    //	return false;
    //    }


    @Override
    public void selectTreeMenuItem(final String name) {
	//TODO must be implemented later when the global domain tree manager will provide ability to retrieve list of non principle entity centres.
	//	final DynamicEntityReview<T, DAO, R> view = getView().getView();
	//	if (view instanceof DynamicEntityReviewWithTabs) {
	//	    final DynamicEntityReviewWithTabs<T, DAO, R> viewWithTabs = (DynamicEntityReviewWithTabs<T, DAO, R>) view;
	//	    viewWithTabs.selectGridTab();
	//	}
    }

}
