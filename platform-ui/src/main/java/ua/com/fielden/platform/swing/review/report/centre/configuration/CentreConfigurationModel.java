package ua.com.fielden.platform.swing.review.report.centre.configuration;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.ei.development.EntityInspectorModel;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.centre.EntityCentreModel;
import ua.com.fielden.platform.swing.review.report.centre.binder.CentrePropertyBinder;
import ua.com.fielden.platform.swing.review.report.events.CentreConfigurationEvent;
import ua.com.fielden.platform.swing.review.report.events.CentreConfigurationEvent.CentreConfigurationAction;
import ua.com.fielden.platform.swing.review.report.interfaces.ICentreConfigurationEventListener;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;
import ua.com.fielden.platform.swing.savereport.SaveReportDialog;
import ua.com.fielden.platform.swing.savereport.SaveReportDialogModel;
import ua.com.fielden.platform.swing.savereport.SaveReportOptions;

/**
 * Model for entity centre. This model allows one to configure and view report.
 *
 * @author TG Team
 *
 * @param <DTME>
 * @param <T>
 * @param <DAO>
 */
public class CentreConfigurationModel<T extends AbstractEntity<?>> extends AbstractCentreConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer>{

    /**
     * The associated {@link GlobalDomainTreeManager} instance.
     */
    protected final IGlobalDomainTreeManager gdtm;

    private final Action save, closingSave, saveAs, closingSaveAs, remove;

    /**
     * Needed for handling close operation.
     */
    private CentreCloseOption closeOption = CentreCloseOption.NO_OPTION;

    /**
     * Initiates this {@link CentreConfigurationModel} with instance of {@link IGlobalDomainTreeManager}, entity type and {@link EntityFactory}.
     *
     * @param entityType - the entity type for which this {@link CentreConfigurationModel} will be created.
     * @param gdtm - Associated {@link GlobalDomainTreeManager} instance.
     * @param entityFactory - {@link EntityFactory} needed for wizard model creation.
     */
    public CentreConfigurationModel(final Class<T> entityType, final String name, final IGlobalDomainTreeManager gdtm, final EntityFactory entityFactory, final IEntityMasterManager masterManager, final ICriteriaGenerator criteriaGenerator){
	super(entityType, name, entityFactory, masterManager, criteriaGenerator);
	this.gdtm = gdtm;
	this.save = createSaveAction(true, false);
	this.closingSave = createSaveAction(false, true);
	this.saveAs = createSaveAsAction(true, false);
	this.closingSaveAs = createSaveAsAction(false, true);
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

    /**
     * Returns value that indicates whether this configuration model can be close or not.
     *
     * @return
     */
    final boolean canClose() {
	final String title = StringUtils.isEmpty(name) ? TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey() : name;
	acceptAnalysis();
	if(gdtm.isChangedEntityCentreManager(entityType, name)){
	    switch(JOptionPane.showConfirmDialog(null, "Would you like to save changes"
		    + (!StringUtils.isEmpty(title) ? " for the " + title : "") + " before closing?", "Save report", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)){
		    case JOptionPane.YES_OPTION:
			closeOption = CentreCloseOption.SAVE;
			return true;
		    case JOptionPane.NO_OPTION:
			closeOption = CentreCloseOption.DISCARD;
			return true;
		    case JOptionPane.CANCEL_OPTION:
			closeOption = CentreCloseOption.CANCEL;
			return false;
	    };
	}
	closeOption = CentreCloseOption.NO_OPTION;
	return true;
    }

    /**
     * Performs close operation.
     */
    final void close(){
	switch(closeOption){
	case SAVE:
	    if(gdtm.isFreezedEntityCentreManager(entityType, name)){
		gdtm.saveEntityCentreManager(entityType, name);
	    }
	    closingSave.actionPerformed(null);
	    break;
	case DISCARD:
	    if(gdtm.isFreezedEntityCentreManager(entityType, name)){
		gdtm.discardEntityCentreManager(entityType, name);
	    }
	    gdtm.discardEntityCentreManager(entityType, name);
	    break;
	case CANCEL:
	    throw new IllegalStateException("This option must prevent close operation!");
	}
	closeOption = CentreCloseOption.NO_OPTION;
    }

    @Override
    protected EntityCentreModel<T> createEntityCentreModel() {
	final ICentreDomainTreeManagerAndEnhancer cdtme = gdtm.getEntityCentreManager(entityType, name);
	if(cdtme == null || cdtme.getSecondTick().checkedProperties(entityType).isEmpty()){
	    throw new IllegalStateException("The centre manager is not specified");
	}
	return new EntityCentreModel<T>(this, createInspectorModel(criteriaGenerator.generateCentreQueryCriteria(entityType, cdtme)), masterManager, name);
    }

    @Override
    protected DomainTreeEditorModel<T> createDomainTreeEditorModel() {
	final ICentreDomainTreeManagerAndEnhancer cdtm = gdtm.getEntityCentreManager(entityType, name);
	if(cdtm == null){
	    throw new IllegalStateException("The centre manager is not specified");
	}
	return new DomainTreeEditorModel<T>(entityFactory, cdtm, entityType);
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
		return new Result(this, new CanNotSetModeException("Please choose properties to add to the result set!"));
	    }
	}
	return Result.successful(this);
    }

    /**
     * Creates the {@link EntityInspectorModel} for the specified criteria
     *
     * @param criteria
     * @return
     */
    private EntityInspectorModel<EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer,T,IEntityDao<T>>> createInspectorModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer,T,IEntityDao<T>> criteria){
	return new EntityInspectorModel<EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer,T,IEntityDao<T>>>(criteria,//
		CentrePropertyBinder.<T>createCentrePropertyBinder(criteriaGenerator));
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
		return fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, false, null, CentreConfigurationAction.PRE_REMOVE));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		gdtm.removeEntityCentreManager(entityType, name);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, false, null, CentreConfigurationAction.REMOVE));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, false, null, CentreConfigurationAction.POST_REMOVE));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, false, ex, CentreConfigurationAction.REMOVE_FAILED));
	    }
	};
    }

    private Action createSaveAsAction(final boolean acceptAnalysis, final boolean isClosing) {
	return new Command<Void>("Save As") {

	    private static final long serialVersionUID = -1316746113497694217L;

	    private String saveAsName = null;

	    private final SaveReportDialog saveReportDialog = new SaveReportDialog(new SaveReportDialogModel(entityType, gdtm));

	    @Override
	    protected boolean preAction() {
		final boolean result= super.preAction();
		if(!result){
		    return false;
		}
		final boolean shouldSave = SaveReportOptions.APPROVE.equals(saveReportDialog.showDialog());
		if(shouldSave){
		    saveAsName = saveReportDialog.getEnteredFileName();
		    return fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, saveAsName, isClosing, null, CentreConfigurationAction.PRE_SAVE_AS));
		} else {
		    saveAsName = null;
		    return false;
		}
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		if(acceptAnalysis){
		    acceptAnalysis();
		}
		gdtm.saveAsEntityCentreManager(entityType, name, saveAsName);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, saveAsName, isClosing, null, CentreConfigurationAction.SAVE_AS));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, saveAsName, isClosing, null, CentreConfigurationAction.POST_SAVE_AS));

	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, saveAsName, isClosing, ex, CentreConfigurationAction.SAVE_AS_FAILED));
	    }

	};
    }

    private Action createSaveAction(final boolean acceptAnalysis, final boolean isClosing) {
	return new Command<Void>("Save") {

	    private static final long serialVersionUID = 7912294028797678105L;

	    @Override
	    protected boolean preAction() {
		final boolean result= super.preAction();
		if(!result){
		    return false;
		}
		return fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, isClosing, null, CentreConfigurationAction.PRE_SAVE));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		if(acceptAnalysis){
		    acceptAnalysis();
		}
		gdtm.saveEntityCentreManager(entityType, name);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, isClosing, null, CentreConfigurationAction.SAVE));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, isClosing, null, CentreConfigurationAction.POST_SAVE));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationModel.this, null, isClosing, ex, CentreConfigurationAction.SAVE_FAILED));
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

    /**
     * Accepts all analysis associated with this entity centre.
     */
    final void acceptAnalysis(){
	final ICentreDomainTreeManager centreManager = gdtm.getEntityCentreManager(entityType, name);
	if(centreManager != null){
	    for(final String analysis : centreManager.analysisKeys()){
		if(centreManager.isFreezedAnalysisManager(analysis)){
		    centreManager.acceptAnalysisManager(analysis);
		}
		centreManager.acceptAnalysisManager(analysis);
	    }
	}
    }

    /**
     * Represents the centre closing options;
     *
     * @author TG Team
     *
     */
    private enum CentreCloseOption{
	SAVE,
	DISCARD,
	NO_OPTION,
	CANCEL;
    }
}
