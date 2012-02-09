package ua.com.fielden.platform.swing.review.report.centre.configuration;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.centre.EntityCentreModel;
import ua.com.fielden.platform.swing.review.report.events.CentreConfigurationEvent;
import ua.com.fielden.platform.swing.review.report.events.CentreConfigurationEvent.CentreConfigurationAction;
import ua.com.fielden.platform.swing.review.report.interfaces.ICentreConfigurationEventListener;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;

/**
 * Model for entity centre. This model allows one to configure and view report.
 * 
 * @author TG Team
 *
 * @param <DTME>
 * @param <T>
 * @param <DAO>
 */
public class CentreConfigurationModel<T extends AbstractEntity> extends AbstractCentreConfigurationModel<T, ICentreDomainTreeManager>{

    /**
     * The associated {@link GlobalDomainTreeManager} instance.
     */
    protected final IGlobalDomainTreeManager gdtm;

    private final Action save, saveAs, remove;

    /**
     * Initiates this {@link CentreConfigurationModel} with instance of {@link IGlobalDomainTreeManager}, entity type and {@link EntityFactory}.
     * 
     * @param entityType - the entity type for which this {@link CentreConfigurationModel} will be created.
     * @param gdtm - Associated {@link GlobalDomainTreeManager} instance.
     * @param entityFactory - {@link EntityFactory} needed for wizard model creation.
     */
    public CentreConfigurationModel(final Class<T> entityType, final String name, final IGlobalDomainTreeManager gdtm, final EntityFactory entityFactory, final ICriteriaGenerator criteriaGenerator){
	super(entityType, name, entityFactory, criteriaGenerator);
	this.gdtm = gdtm;
	this.save = createSaveAction();
	this.saveAs = createSaveAsAction();
	this.remove = createRemoveAction();
    }

    /**
     * Saves this configuration.
     */
    public void save(){
	save.actionPerformed(null);
    }

    /**
     * Saves as this configuration.
     */
    public void saveAs(){
	saveAs.actionPerformed(null);
    }

    /**
     * Removes this configuration.
     */
    public void remove(){
	remove.actionPerformed(null);
    }

    /**
     * Registers the {@link ICentreConfigurationEventListener} to listen the centre configuration event.
     * 
     * @param l
     */
    public void addCentreConfigurationEventListener(final ICentreConfigurationEventListener l){
	listenerList.add(ICentreConfigurationEventListener.class, l);
    }

    /**
     * Removes the specified {@link ICentreConfigurationEventListener} from the list of registered listeners.
     * 
     * @param l
     */
    public void removeCentreConfigurationEventListener(final ICentreConfigurationEventListener l){
	listenerList.remove(ICentreConfigurationEventListener.class, l);
    }

    @Override
    protected EntityCentreModel<T> createEntityCentreModel() {
	final ICentreDomainTreeManager cdtm = gdtm.getEntityCentreManager(entityType, name);
	if(cdtm == null || cdtm.getSecondTick().checkedProperties(entityType).isEmpty()){
	    throw new IllegalStateException("The centre manager is not specified");
	}
	return new EntityCentreModel<T>(this, criteriaGenerator.generateCentreQueryCriteria(entityType, cdtm), name);
    }

    @Override
    protected DomainTreeEditorModel<T> createDomainTreeEditorModel() {
	final ICentreDomainTreeManager cdtm = gdtm.getEntityCentreManager(entityType, name);
	if(cdtm == null){
	    throw new IllegalStateException("The centre manager is not specified");
	}
	return new DomainTreeEditorModel<T>(entityFactory, gdtm.getEntityCentreManager(entityType, name), entityType);
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(ReportMode.REPORT.equals(mode)){
	    ICentreDomainTreeManager cdtm = gdtm.getEntityCentreManager(entityType, name);
	    if(cdtm == null){
		gdtm.initEntityCentreManager(entityType, name);
		cdtm = gdtm.getEntityCentreManager(entityType, name);
	    }
	    if(cdtm == null){
		return new Result(this, new Exception("The entity centre must be initialized!"));
	    }
	    if(cdtm.getSecondTick().checkedProperties(entityType).isEmpty()){
		return new Result(this, new CanNotSetModeException("This report is opened for the first time!"));
	    }
	}
	return Result.successful(this);
    }

    private Action createRemoveAction() {
	return new Command<Void>("Remove") {

	    private static final long serialVersionUID = -1316746113497694217L;

	    @Override
	    protected boolean preAction() {
		final boolean result = super.preAction();
		if(!result){
		    return false;
		}
		return fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, CentreConfigurationAction.PRE_REMOVE));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		gdtm.removeEntityCentreManager(entityType, name);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, CentreConfigurationAction.REMOVE));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, CentreConfigurationAction.POST_REMOVE));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, CentreConfigurationAction.REMOVE_FAILED));
	    }
	};
    }

    private Action createSaveAsAction() {
	return new Command<Void>("Save As") {

	    private static final long serialVersionUID = -1316746113497694217L;

	    private String saveAsName = null;

	    @Override
	    protected boolean preAction() {
		//TODO Must provide save as dialog
		final boolean result= super.preAction();
		if(!result){
		    return false;
		}
		return fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, saveAsName, CentreConfigurationAction.PRE_SAVE_AS));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		gdtm.saveAsEntityCentreManager(entityType, name, saveAsName);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, saveAsName, CentreConfigurationAction.SAVE_AS));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, saveAsName, CentreConfigurationAction.POST_SAVE_AS));

	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, saveAsName, CentreConfigurationAction.SAVE_AS_FAILED));
	    }

	};
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
		return fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, CentreConfigurationAction.PRE_SAVE));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		gdtm.saveEntityCentreManager(entityType, name);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, CentreConfigurationAction.SAVE));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, CentreConfigurationAction.POST_SAVE));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, CentreConfigurationAction.SAVE_FAILED));
	    }
	};
    }


    /**
     * Iterates through the list of {@link ICentreConfigurationEventListener} listeners and delegates the event to every listener.
     * 
     * @param event
     * 
     * @return
     */
    private boolean fireCentreConfigurationEvent(final CentreConfigurationEvent event){
	boolean result = true;
	for(final ICentreConfigurationEventListener listener : listenerList.getListeners(ICentreConfigurationEventListener.class)){
	    result &= listener.centerConfigurationEventPerformed(event);
	}
	return result;
    }
    //    /**
    //     * Returns the {@link IGlobalDomainTreeManager} instance associated with this centre configuration model.
    //     *
    //     * @return
    //     */
    //    public GlobalDomainTreeManager gdtm(){
    //	return gdtm;
    //    }
    //
    //    /**
    //     * Returns value that indicates the current centre's mode: WIZARD or REPORT.
    //     *
    //     * @return
    //     */
    //    public ReportMode getMode() {
    //	return mode;
    //    }

    //    public void open(){
    //
    //    }
}
