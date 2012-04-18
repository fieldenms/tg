package ua.com.fielden.platform.swing.review.report.centre.configuration;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.ei.development.EntityInspectorModel;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.centre.EntityCentreModel;
import ua.com.fielden.platform.swing.review.report.centre.binder.CentrePropertyBinder;
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
public class CentreConfigurationModel<T extends AbstractEntity<?>> extends AbstractCentreConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer>{

    /**
     * The associated {@link GlobalDomainTreeManager} instance.
     */
    private final IGlobalDomainTreeManager gdtm;

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
    }

    /**
     * Saves this configuration.
     */
    public void save(){
	gdtm.saveEntityCentreManager(getEntityType(), getName());
    }

    /**
     * Discards changes in the entity centre.
     */
    public void discard(){
	gdtm.discardEntityCentreManager(getEntityType(), getName());
    }

    /**
     * Saves as this configuration.
     */
    public void saveAs(final String saveAsName){
	gdtm.saveAsEntityCentreManager(getEntityType(), getName(), saveAsName);
    }

    /**
     * Removes this configuration.
     */
    public void remove(){
	gdtm.removeEntityCentreManager(getEntityType(), getName());
    }

    /**
     * Returns the value that indicates whether this entity centre has changed or not.
     * 
     * @return
     */
    public boolean isChanged(){
	return gdtm.isChangedEntityCentreManager(getEntityType(), getName());
    }

    /**
     * Returns value that indicates whether this entity centre is freezed or not.
     * 
     * @return
     */
    public boolean isFreezed(){
	return gdtm.isFreezedEntityCentreManager(getEntityType(), getName());
    }

    /**
     * Freezes the associated entity centre model.
     */
    public void freez(){
	gdtm.freezeEntityCentreManager(getEntityType(), getName());
    }

    /**
     * Returns the entity centre manager for this centre configuration model.
     * 
     * @return
     */
    public ICentreDomainTreeManagerAndEnhancer getEntityCentreManager(){
	return gdtm.getEntityCentreManager(getEntityType(), getName());
    }

    /**
     * Initialises the entity centre.
     */
    public void initEntityCentreManager(){
	gdtm.initEntityCentreManager(getEntityType(), getName());
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
     * Returns the list of non principle entity centre list.
     * 
     * @return
     */
    public List<String> getNonPrincipleEntityCentreList(){
	return new ArrayList<String>();
    }

    @Override
    protected EntityCentreModel<T> createEntityCentreModel() {
	final ICentreDomainTreeManagerAndEnhancer cdtme = getEntityCentreManager();
	if(cdtme == null || cdtme.getSecondTick().checkedProperties(getEntityType()).isEmpty()){
	    throw new IllegalStateException("The centre manager is not specified");
	}
	return new EntityCentreModel<T>(createInspectorModel(getCriteriaGenerator().generateCentreQueryCriteria(getEntityType(), cdtme)), getMasterManager(), getName());
    }

    @Override
    protected DomainTreeEditorModel<T> createDomainTreeEditorModel() {
	final ICentreDomainTreeManagerAndEnhancer cdtm = getEntityCentreManager();
	if(cdtm == null){
	    throw new IllegalStateException("The centre manager is not specified");
	}
	return new DomainTreeEditorModel<T>(getEntityFactory(), cdtm, getEntityType());
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(ReportMode.REPORT.equals(mode)){
	    final ICentreDomainTreeManager cdtm = getEntityCentreManager();
	    if(cdtm == null){
		throw new IllegalStateException("The entity centre must be initialized!");
	    }
	    if(cdtm.getSecondTick().checkedProperties(getEntityType()).isEmpty()){
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
		CentrePropertyBinder.<T>createCentrePropertyBinder(getCriteriaGenerator()));
    }

}
