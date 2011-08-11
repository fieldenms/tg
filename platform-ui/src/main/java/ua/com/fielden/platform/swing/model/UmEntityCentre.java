package ua.com.fielden.platform.swing.model;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicEntityReviewModel;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.LocatorPersistentObject;
import ua.com.fielden.platform.swing.review.PropertyPersistentObject;
import ua.com.fielden.platform.swing.review.optionbuilder.ActionChangerBuilder;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.UvEntityCentre;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * A common model for dynamic entity centres, which provides the following functionality:
 * <ul>
 * <li>an action to create an entity master for editing of a selected in EGI entity;
 * <li>a dbl-click mouse event listener performing the above action;
 * <li>an action to create an entity master for creation of a new entity;
 * <ul>
 * 
 * @author TG Teams
 * 
 */
public abstract class UmEntityCentre<T extends AbstractEntity, DAO extends IEntityDao<T>, F extends BaseFrame> extends DynamicEntityReviewModel<T, DAO, T> implements IOpenGuard {

    /**
     * A convenient blocking provider for supporting actions with blocking layer in a lazy manner (i.e. when view is actually set for the model).
     */
    private final IBlockingLayerProvider blockingLayerProvider;
    private final ActionPanelBuilder actionPanelBuilder;

    private final DAO controller;
    private final EntityFactory entityFactory;

    private UvEntityCentre<T, DAO, F, ?> view;

    private final IEntityProducer<T> entityProducer;

    public UmEntityCentre(//
    final IEntityProducer<T> entityProducer, //
    final EntityFactory entityFactory,//
    final DynamicEntityQueryCriteria<T, DAO> criteria,//
    final PropertyTableModelBuilder<T> builder,//
    final Map<String, PropertyPersistentObject> criteriaProperties,//
    final ActionChangerBuilder actionChangerBuilder,//
    final ActionPanelBuilder panelBuilder, //
    final int columns, final IEntityMasterManager entityMasterFactory, final ILocatorConfigurationController locatorController,//
    final LocatorPersistentObject locatorPersistentObject, final Runnable... afterRunActions) {
	super(criteria, builder, actionChangerBuilder, locatorController, locatorPersistentObject, columns, criteriaProperties, entityMasterFactory, afterRunActions);

	this.controller = criteria.getDao();
	this.entityFactory = entityFactory;
	this.entityProducer = entityProducer;

	// a convenient blocking provider for supporting actions with blocking layer in a lazy manner (i.e. when view is actually set for the model)
	blockingLayerProvider = new IBlockingLayerProvider() {
	    @Override
	    public BlockingIndefiniteProgressLayer getBlockingLayer() {
		return getView() != null && getView().getReviewContract() != null ? getView().getReviewContract().getBlockingLayer() : null;
	    }
	};

	actionPanelBuilder = new ActionPanelBuilder()//
	.addButton(createOpenMasterWithNewCommand())//
	.addButton(createOpenMasterCommand())//
	.addSeparator()//
	.addActionItems(panelBuilder);
    }

    /**
     * Returns a default action panel builder with actions open master and open master with new command.
     */
    @Override
    public ActionPanelBuilder getActionPanelBuilder() {
	return actionPanelBuilder;
    }

    protected Command<T> createOpenMasterWithNewCommand() {
	final Command<T> action = new Command<T>("New") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected T action(final ActionEvent event) throws Exception {
		return entityProducer.newEntity();
	    }

	    @Override
	    protected void postAction(final T entity) {
		getEntityMasterFactory().<T, DAO> showMaster(entity, getView());
		super.postAction(entity);
	    }
	};
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_1);
	action.putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/document-new.png"));
	action.putValue(Action.SMALL_ICON, ResourceLoader.getIcon("images/document-new.png"));
	action.putValue(Action.SHORT_DESCRIPTION, "New");
	action.setEnabled(true);
	return action;
    }

    /**
     * A command that creates and opens an entity master frame for the selected in the EGI entity.
     * 
     * @return
     */
    protected Command<T> createOpenMasterCommand() {
	final Command<T> action = new BlockingLayerCommand<T>("Edit", blockingLayerProvider) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		setMessage("Opening...");
		return super.preAction();
	    }

	    @Override
	    protected T action(final ActionEvent event) throws Exception {
		return getSelectedEntity();
	    }

	    @Override
	    protected void postAction(final T entity) {
		super.postAction(entity);
		if (entity != null) {
		    getEntityMasterFactory().<T, DAO> showMaster(entity, getView());
		}
	    }
	};
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_2);
	action.putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/document-edit.png"));
	action.putValue(Action.SMALL_ICON, ResourceLoader.getIcon("images/document-edit.png"));
	action.putValue(Action.SHORT_DESCRIPTION, "Edit");
	action.setEnabled(true);
	return action;
    }

    protected Command<T> createDeleteCommand() {
	final Command<T> action = new BlockingLayerCommand<T>("Delete", getBlockingLayerProvider()) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		final T entity = getSelectedEntity();
		if (entity == null) { // there is nothing to delete
		    return false;
		}

		if (JOptionPane.showConfirmDialog(getEntityReview(), "Entity " + entity + " will be deleted. Proceed?", "Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
		    setMessage("Deleting...");
		    return super.preAction();
		} else {
		    return false;

		}
	    }

	    @Override
	    protected T action(final ActionEvent event) throws Exception {
		final T entity = getSelectedEntity();
		getController().delete(entity);
		return entity;
	    }

	    @Override
	    protected void postAction(final T entity) {
		getTableModel().removeInstances(entity);
		getTableModel().fireTableDataChanged();
		super.postAction(entity);
	    }

	};
	action.setEnabled(true);
	action.putValue(Action.SHORT_DESCRIPTION, "Delete");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_3);
	action.putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/document-delete.png"));
	action.putValue(Action.SMALL_ICON, ResourceLoader.getIcon("images/document-delete.png"));
	return action;
    }

    public DAO getController() {
	return controller;
    }

    public UvEntityCentre<T, DAO, F, ?> getView() {
	return view;
    }

    public void setView(final UvEntityCentre<T, DAO, F, ?> view) {
	this.view = view;
    }

    @Override
    public boolean canOpen() {
	return true;
    }

    @Override
    public String whyCannotOpen() {
	return "Should be able to open by default";
    }

    public EntityFactory getEntityFactory() {
	return entityFactory;
    }

    public IEntityProducer<T> getEntityProducer() {
	return entityProducer;
    }

    /**
     * The returned blocking layer provider can be safely used for implementing custom actions with EGI blocking.
     * 
     * @return
     */
    public IBlockingLayerProvider getBlockingLayerProvider() {
	return blockingLayerProvider;
    }

}
