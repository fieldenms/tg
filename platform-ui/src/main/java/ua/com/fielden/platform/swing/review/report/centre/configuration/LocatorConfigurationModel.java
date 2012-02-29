package ua.com.fielden.platform.swing.review.report.centre.configuration;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.ILocatorManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeRepresentation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.ei.EntityInspectorModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.centre.EntityLocatorModel;
import ua.com.fielden.platform.swing.review.report.centre.binder.CentrePropertyBinder;
import ua.com.fielden.platform.swing.review.report.centre.binder.CentrePropertyBinder.EntityCentreType;
import ua.com.fielden.platform.swing.review.report.events.LocatorConfigurationEvent;
import ua.com.fielden.platform.swing.review.report.events.LocatorConfigurationEvent.LocatorConfigurationAction;
import ua.com.fielden.platform.swing.review.report.interfaces.ILocatorConfigurationEventListener;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;

public class LocatorConfigurationModel<T extends AbstractEntity, R extends AbstractEntity> extends AbstractCentreConfigurationModel<T, ILocatorDomainTreeManager> {

    /**
     * The class where the property specified with propertyName was declared.
     */
    public final Class<R> rootType;
    /**
     * The associated {@link ILocatorManager} instance.
     */
    public final ILocatorManager locatorManager;

    private final Action save, saveAsDefault, loadDefault;
    /**
     * Initiates this {@link LocatorConfigurationModel} with instance of {@link IGlobalDomainTreeRepresentation}, entity type and {@link EntityFactory}.
     * 
     * @param entityType - The entity type for which this {@link CentreConfigurationModel} will be created.
     * @param rootType - The entity type where the property specified with property name was declared.
     * @param gdtr - Associated {@link GlobalDomainTreeRepresentation} instance.
     * @param entityFactory - {@link EntityFactory} needed for wizard model creation.
     */
    public LocatorConfigurationModel( final Class<T> entityType, final Class<R> rootType, final String propertyName, final ILocatorManager locatorManager, final EntityFactory entityFactory, final ICriteriaGenerator criteriaGenerator){
	super(entityType, propertyName, entityFactory, criteriaGenerator);
	this.rootType = rootType;
	this.locatorManager = locatorManager;
	this.save = createSaveAction();
	this.saveAsDefault = createSaveAsDefaultAction();
	this.loadDefault = createLoadDefaultAction();
    }

    /**
     * Saves this locator's configuration.
     */
    public void save(){
	save.actionPerformed(null);
    }

    /**
     * Saves this locator's configuration as default and saves it locally.
     */
    public void saveAsDefault(){
	saveAsDefault.actionPerformed(null);
    }

    /**
     * Loads default locator's configuration.
     */
    public void loadDefault(){
	loadDefault.actionPerformed(null);
    }

    /**
     * Registers the {@link ILocatorConfigurationEventListener} to listen the locator configuration event.
     * 
     * @param l
     */
    public void addLocatorConfigurationEventListener(final ILocatorConfigurationEventListener l){
	listenerList.add(ILocatorConfigurationEventListener.class, l);
    }

    /**
     * Removes the specified {@link ILocatorConfigurationEventListener} from the list of registered listeners.
     * 
     * @param l
     */
    public void removeLocatorConfigurationEventListener(final ILocatorConfigurationEventListener l){
	listenerList.remove(ILocatorConfigurationEventListener.class, l);
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(ReportMode.REPORT.equals(mode)){
	    ILocatorDomainTreeManager ldtm = locatorManager.getLocatorManager(rootType, name);
	    if(ldtm == null){
		locatorManager.initLocatorManagerByDefault(rootType, name);
		ldtm = locatorManager.getLocatorManager(rootType, name);
	    }
	    if(ldtm == null){
		return new Result(this, new Exception("The locator manager must be initialised"));
	    }
	    if(ldtm.getSecondTick().checkedProperties(entityType).isEmpty()){
		return new Result(this, new CanNotSetModeException("This report is opened for the first time!"));
	    }
	}
	return Result.successful(this);
    }

    @Override
    protected EntityLocatorModel<T> createEntityCentreModel() {
	final ILocatorDomainTreeManager ldtm = locatorManager.getLocatorManager(rootType, name);
	if(ldtm == null || ldtm.getSecondTick().checkedProperties(entityType).isEmpty()){
	    throw new IllegalStateException("The locator manager is not specified correctly!");
	}
	return new EntityLocatorModel<T>(this, createInspectorModel(criteriaGenerator.generateLocatorQueryCriteria(entityType, ldtm)), name);
    }

    @Override
    protected DomainTreeEditorModel<T> createDomainTreeEditorModel() {
	final ILocatorDomainTreeManagerAndEnhancer ldtm = locatorManager.getLocatorManager(rootType, name);
	if(ldtm == null){
	    throw new IllegalStateException("The locator manager can not be null!");
	}
	return new DomainTreeEditorModel<T>(entityFactory, ldtm, entityType);
    }

    /**
     * Creates the {@link EntityInspectorModel} for the specified criteria
     * 
     * @param criteria
     * @return
     */
    private EntityInspectorModel<EntityQueryCriteria<ILocatorDomainTreeManager,T,IEntityDao<T>>> createInspectorModel(final EntityQueryCriteria<ILocatorDomainTreeManager,T,IEntityDao<T>> criteria){
	return new EntityInspectorModel<EntityQueryCriteria<ILocatorDomainTreeManager,T,IEntityDao<T>>>(criteria,//
		new CentrePropertyBinder<EntityQueryCriteria<ILocatorDomainTreeManager,T,IEntityDao<T>>>(EntityCentreType.LOCATOR, criteriaGenerator));
    }

    private Action createSaveAction() {
	return new Command<Void>("Save") {

	    private static final long serialVersionUID = 7912294028797678105L;

	    @Override
	    protected boolean preAction() {
		final boolean result= super.preAction();
		if(!result){
		    return false;
		}
		return fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationModel.this, LocatorConfigurationAction.PRE_SAVE));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		locatorManager.acceptLocatorManager(entityType, name);
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationModel.this, LocatorConfigurationAction.SAVE));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationModel.this,  LocatorConfigurationAction.POST_SAVE));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationModel.this, LocatorConfigurationAction.SAVE_FAILED));
	    }
	};
    }

    private Action createSaveAsDefaultAction() {
	return new Command<Void>("Save As Default") {

	    private static final long serialVersionUID = 7462084429292050025L;

	    @Override
	    protected boolean preAction() {
		final boolean result= super.preAction();
		if(!result){
		    return false;
		}
		return fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationModel.this, LocatorConfigurationAction.PRE_SAVE_AS_DEFAULT));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		locatorManager.saveLocatorManagerGlobally(entityType, name);
		locatorManager.acceptLocatorManager(entityType, name);
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationModel.this, LocatorConfigurationAction.SAVE_AS_DEFAULT));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationModel.this, LocatorConfigurationAction.POST_SAVE_AS_DEFAULT));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationModel.this, LocatorConfigurationAction.SAVE_AS_DEFAULT_FAILED));
	    }
	};
    }

    private Action createLoadDefaultAction() {
	return new Command<Void>("Load Default") {

	    private static final long serialVersionUID = -1337109555032877767L;

	    @Override
	    protected boolean preAction() {
		final boolean result= super.preAction();
		if(!result){
		    return false;
		}
		return fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationModel.this, LocatorConfigurationAction.PRE_LOAD_DEFAULT));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		locatorManager.initLocatorManagerByDefault(entityType, name);
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationModel.this, LocatorConfigurationAction.LOAD_DEFAULT));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationModel.this, LocatorConfigurationAction.POST_LOAD_DEFAULT));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationModel.this, LocatorConfigurationAction.LOAD_DEFAULT_FAILED));
	    }
	};
    }



    /**
     * Iterates through the list of {@link ILocatorConfigurationEventListener} listeners and delegates the event to every listener.
     * 
     * @param event
     * 
     * @return
     */
    private boolean fireLocatorConfigurationEvent(final LocatorConfigurationEvent event){
	boolean result = true;
	for(final ILocatorConfigurationEventListener listener : listenerList.getListeners(ILocatorConfigurationEventListener.class)){
	    result &= listener.locatorConfigurationEventPerformed(event);
	}
	return result;
    }
}
