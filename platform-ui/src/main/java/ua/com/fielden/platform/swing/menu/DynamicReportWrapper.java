package ua.com.fielden.platform.swing.menu;

import javax.swing.JOptionPane;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.model.DefaultUiModel;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.CentreConfigurationEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ICentreConfigurationEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ICentreConfigurationFactory;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;

/**
 * Ad hoc report wrapper. See {@link BaseNotifPanel} for more information.
 * 
 * @author TG Team
 * 
 */
public class DynamicReportWrapper<T extends AbstractEntity<?>> extends BaseNotifPanel<DefaultUiModel> {

    private static final long serialVersionUID = 1655601830703524962L;

    //private final EventListenerList listenerList = new EventListenerList();

    //Menu item related properties.
    private final TreeMenuWithTabs<?> treeMenu;
    private final String description;

    //Entity centre related properties.
    private final IGlobalDomainTreeManager gdtm;
    private final EntityFactory entityFactory;
    private final IEntityMasterManager masterManager;
    private final ICriteriaGenerator criteriaGenerator;
    private final ICentreConfigurationFactory<T> centreFactory;
    private final CentreConfigurationView<T, ?> entityCentreConfigurationView;

    /**
     * Creates new {@link DynamicReportWrapper} for the given {@link DynamicCriteriaModelBuilder} and with specified title and information about the wrapped report.
     * 
     * @param caption
     * @param description
     * @param modelBuilder
     */
    public DynamicReportWrapper(
	    //Menu item related parameters
	    final String caption,//
	    final String description,//
	    final TreeMenuWithTabs<?> treeMenu,//
	    //Entity centre related parameters
	    final String name,//
	    final Class<? extends MiWithConfigurationSupport<T>> menuItemClass,//
		    final ICentreConfigurationFactory<T> centreFactory,//
		    final IGlobalDomainTreeManager gdtm,//
		    final EntityFactory entityFactory,//
		    final IEntityMasterManager masterManager,//
		    final ICriteriaGenerator criteriaGenerator) {
	super(caption, new DefaultUiModel(true));
	this.description = description;
	this.treeMenu = treeMenu;
	this.gdtm = gdtm;
	this.entityFactory = entityFactory;
	this.masterManager = masterManager;
	this.criteriaGenerator = criteriaGenerator;
	this.centreFactory = centreFactory;
	//Create and configure entity centre;
	//final CentreConfigurationModel<T> configModel = new CentreConfigurationModel<T>(entityType, name, gdtm, entityFactory, masterManager, criteriaGenerator);
	final BlockingIndefiniteProgressLayer progressLayer = new BlockingIndefiniteProgressLayer(null, "");
	this.entityCentreConfigurationView = centreFactory.createCentreConfigurationView(menuItemClass, name, gdtm, entityFactory, masterManager, criteriaGenerator, progressLayer);
	this.entityCentreConfigurationView.getModel().addCentreConfigurationEventListener(createContreConfigurationListener());
	//new MultipleAnalysisEntityCentreConfigurationView<T>(configModel, progressLayer);
	progressLayer.setView(entityCentreConfigurationView);
	add(progressLayer);
	getModel().setView(this);

    }

    //	final DynamicCriteriaModelBuilder<T, DAO, R> newCriteriaModelBuilder = getDynamicCriteriaModelBuilderFor(getKeyToSave(), saveReportDialog.getEnteredFileName());
    //		final MiSaveAsConfiguration<T, DAO, R> newTreeMenuItem = new MiSaveAsConfiguration<T, DAO, R>(saveReportDialog.getEnteredFileName(), getView().getInfo(), newCriteriaModelBuilder, treeMenu);
    //		newTreeMenuItem.getView().setSaveAction(newCriteriaModelBuilder.createSaveAction());
    //		newTreeMenuItem.getView().setSaveAsAction(createSaveAsAction(newCriteriaModelBuilder));
    //		newTreeMenuItem.getView().setRemoveAction(createRemoveAction(newTreeMenuItem));
    //		newTreeMenuItem.getView().setPanelBuilder(createAnalysisActionPanel(newTreeMenuItem));
    //		addItem(newTreeMenuItem);
    //		treeMenu.getModel().getOriginModel().reload(MiWithConfigurationSupport.this);
    //		if (!isClosing) {
    //		    treeMenu.activateOrOpenItem(newTreeMenuItem);
    //		}

    @Override
    public void buildUi() {
	treeMenu.updateSelectedMenuItem();
	entityCentreConfigurationView.open();
    }

    @Override
    public String getInfo() {
	return description;
    }

    //    /**
    //     * Returns value that indicates whether wrapper holds review or wizard model.
    //     *
    //     * @return
    //     */
    //    public boolean isReview() {
    //	return getHoldingPanel().getComponent(0) instanceof DynamicEntityReview ? true : false;
    //    }

    @Override
    public ICloseGuard canClose() {
	return entityCentreConfigurationView.canClose();
    }

    //TODO The notify method must be override and mast take care of cases when entity centre is closing.
    //    @Override
    //    public void notify(final String message, final MessageType messageType) {
    //	super.notify(message, messageType);
    //    }

    @Override
    public void close() {
	entityCentreConfigurationView.close();
	//fireCentreClosingEvent(new CentreClosingEvent(this));
    }

    @Override
    public boolean canLeave() {
	return true;
    }

    public final Class<? extends MiWithConfigurationSupport<T>> getMenuItemClass() {
	//TODO must finish the implementation. This method must return the menu item type, for which the entity centre was created.
	return null;
    }

    public final IGlobalDomainTreeManager getGlobalDomainTreeManager(){
	return gdtm;
    }

    public EntityFactory getEntityFactory() {
	return entityFactory;
    }

    public IEntityMasterManager getMasterManager() {
	return masterManager;
    }

    public ICriteriaGenerator getCriteriaGenerator() {
	return criteriaGenerator;
    }

    public ICentreConfigurationFactory<T> getCentreFactory() {
	return centreFactory;
    }

    public TreeMenuWithTabs<?> getTreeMenu() {
	return treeMenu;
    }

    //	final DynamicCriteriaModelBuilder<T, DAO, R> newCriteriaModelBuilder = getDynamicCriteriaModelBuilderFor(getKeyToSave(), saveReportDialog.getEnteredFileName());
    //		final MiSaveAsConfiguration<T, DAO, R> newTreeMenuItem = new MiSaveAsConfiguration<T, DAO, R>(saveReportDialog.getEnteredFileName(), getView().getInfo(), newCriteriaModelBuilder, treeMenu);
    //		newTreeMenuItem.getView().setSaveAction(newCriteriaModelBuilder.createSaveAction());
    //		newTreeMenuItem.getView().setSaveAsAction(createSaveAsAction(newCriteriaModelBuilder));
    //		newTreeMenuItem.getView().setRemoveAction(createRemoveAction(newTreeMenuItem));
    //		newTreeMenuItem.getView().setPanelBuilder(createAnalysisActionPanel(newTreeMenuItem));
    //		addItem(newTreeMenuItem);
    //		treeMenu.getModel().getOriginModel().reload(MiWithConfigurationSupport.this);
    //		if (!isClosing) {
    //		    treeMenu.activateOrOpenItem(newTreeMenuItem);
    //		}

    private ICentreConfigurationEventListener createContreConfigurationListener() {
	return new ICentreConfigurationEventListener() {

	    @Override
	    public boolean centerConfigurationEventPerformed(final CentreConfigurationEvent event) {
		switch (event.getEventAction()) {
		case POST_SAVE_AS:
		    final MiWithConfigurationSupport<T> principleEntityCentreMenuItem = getPrincipleEntityCentreMenuItem();
		    final MiSaveAsConfiguration<T> newTreeMenuItem = new MiSaveAsConfiguration<T>(//
			    principleEntityCentreMenuItem,//
			    event.getSaveAsName());
		    principleEntityCentreMenuItem.addItem(newTreeMenuItem);
		    treeMenu.getModel().getOriginModel().reload(principleEntityCentreMenuItem);
		    //		    if (!isClosing) {
		    //			treeMenu.activateOrOpenItem(newTreeMenuItem);
		    //		    }
		    break;
		case POST_REMOVE:
		    if (event.getSource().getModel().getName() != null) {
			treeMenu.closeCurrentTab();
			treeMenu.getModel().getOriginModel().removeNodeFromParent(getAssociatedTreeMenuItem());
		    } else {
			throw new IllegalStateException("The principle tree menu item can not be removed!");
		    }
		    break;
		case REMOVE_FAILED:
		    JOptionPane.showMessageDialog(DynamicReportWrapper.this, event.getException().getMessage(), "Information", JOptionPane.INFORMATION_MESSAGE);
		    break;
		}
		return true;
	    }

	};
    }

    @SuppressWarnings("unchecked")
    private MiWithConfigurationSupport<T> getPrincipleEntityCentreMenuItem() {
	final TreeMenuItem<?> associatedTreeMenuItem = getAssociatedTreeMenuItem();
	if (associatedTreeMenuItem instanceof MiWithConfigurationSupport) {
	    return (MiWithConfigurationSupport<T>) associatedTreeMenuItem;
	} else if (associatedTreeMenuItem instanceof MiSaveAsConfiguration) {
	    return (MiWithConfigurationSupport<T>) associatedTreeMenuItem.getParent();
	} else {
	    throw new IllegalStateException("The associated or parent item must be instance of MiWithConfigurationSupport");
	}
    }

    //    public void addCentreClosingListener(final CentreClosingListener l) {
    //	listenerList.add(CentreClosingListener.class, l);
    //    }
    //
    //    public void removeCentreClosingListener(final CentreClosingListener l) {
    //	listenerList.remove(CentreClosingListener.class, l);
    //    }
    //
    //    protected void fireCentreClosingEvent(final CentreClosingEvent event){
    //	final Object[] listeners = listenerList.getListenerList();
    //	// Process the listeners last to first, notifying
    //	// those that are interested in this event
    //	for (int i = listeners.length - 2; i >= 0; i -= 2) {
    //	    if (listeners[i] == CentreClosingListener.class) {
    //		((CentreClosingListener) listeners[i + 1]).centreClosing(event);
    //	    }
    //	}
    //    }

    //    /**
    //     * Contract for anything that is interested in receiving {@link CentreClosingEvent}.
    //     *
    //     * @author TG Team
    //     *
    //     */
    //    public static interface CentreClosingListener extends EventListener{
    //
    //	/**
    //	 * Invoked after centre was closed to perform custom additional task.
    //	 *
    //	 * @param event
    //	 */
    //	void centreClosing(final CentreClosingEvent event);
    //    }
    //
    //    /**
    //     * Represents centre closing event.
    //     *
    //     * @author TG Team
    //     *
    //     */
    //    public static class CentreClosingEvent extends EventObject {
    //
    //	private static final long serialVersionUID = 7671961023819217439L;
    //
    //	/**
    //	 * Constructs centre closing event.
    //	 *
    //	 * @param source
    //	 */
    //	public CentreClosingEvent(final Object source) {
    //	    super(source);
    //	}
    //
    //    }
}
