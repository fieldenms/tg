package ua.com.fielden.platform.swing.model;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.ei.CriteriaInspectorModel;
import ua.com.fielden.platform.swing.ei.editors.IPropertyBinder;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.EntityReviewModel;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.UvCustomEntityCentre;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * A common model for custom entity centres, which provides the following functionality:
 * <ul>
 * <li>an action to create an entity master for editing of a selected in EGI entity;
 * <li>a dbl-click mouse event listener performing the above action;
 * <li>an action to create an entity master for creation of a new entity;
 * <li>manages spawned entity masters.
 * <ul>
 * 
 * @author TG Teams
 * 
 */
public abstract class UmCustomEntityCentre<T extends AbstractEntity, DAO extends IEntityDao<T>, CRIT extends EntityQueryCriteria<T, DAO>, F extends BaseFrame> extends EntityReviewModel<T, DAO, CRIT> implements IOpenGuard {

    private final Command<T> openMasterCommand;
    private final Command<T> openMasterWithNewCommand;

    private final DAO controller;

    private final IBlockingLayerProvider blockingLayerProvider;
    private UvCustomEntityCentre<T, DAO, CRIT, F, ?> view;
    /** Responsible for creation of new entity instances */
    private final IEntityProducer<T> entityProducer;

    public UmCustomEntityCentre(//
    final IEntityProducer<T> entityProducer,//
    final CRIT criteria,//
    final DAO controller,//
    final PropertyTableModelBuilder<T> builder,//
    final IPropertyBinder<CRIT> propertyBinder, final IEntityMasterManager entityMasterFactory) {
	super(criteria, builder, propertyBinder, entityMasterFactory);

	this.entityProducer = entityProducer;
	this.controller = controller;

	// a convenient blocking provider for supporting actions with blocking layer in a lazy manner (i.e. when view is actually set for the model)
	blockingLayerProvider = new IBlockingLayerProvider() {
	    @Override
	    public BlockingIndefiniteProgressLayer getBlockingLayer() {
		return getView() != null && getView().getReviewContract() != null ? getView().getReviewContract().getBlockingLayer() : null;
	    }
	};

	openMasterCommand = createOpenMasterCommand();
	openMasterWithNewCommand = createOpenMasterWithNewCommand();
    }

    @Override
    protected CriteriaInspectorModel<T, DAO, CRIT> createInspectorModel(final CRIT criteria) {
	criteria.getProperty("key").setVisible(false);
	criteria.getProperty("desc").setVisible(false);

	return new CriteriaInspectorModel<T, DAO, CRIT>(criteria, getPropertyBinder());
    }

    /**
     * Should be implemented to provide a custom logic for instantiation of entity specific master frame. Is used for new and existing entities.
     * 
     * @param hook
     * @return
     */
    protected abstract F createFrame(final T entity);

    protected Command<T> createOpenMasterWithNewCommand() {
	final Command<T> action = new Command<T>("New") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		if (super.preAction()) {
		    return entityProducer != null;
		}
		return false;
	    }

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

    /** Should be overridden if the default double click action is not suitable. */
    protected MouseListener createOpenWoMasterDoubleClickAction() {
	return new MouseAdapter() {
	    @Override
	    public void mouseClicked(final MouseEvent e) {
		if (e.getClickCount() == 2) {
		    openMasterCommand.actionPerformed(null);
		}
	    }
	};
    }

    public Command<T> getOpenMasterCommand() {
	return openMasterCommand;
    }

    public DAO getController() {
	return controller;
    }

    public Command<T> getOpenMasterWithNewCommand() {
	return openMasterWithNewCommand;
    }

    public UvCustomEntityCentre<T, DAO, CRIT, F, ?> getView() {
	return view;
    }

    public void setView(final UvCustomEntityCentre<T, DAO, CRIT, F, ?> view) {
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

    public IEntityProducer<T> getEntityProducer() {
	return entityProducer;
    }

}
