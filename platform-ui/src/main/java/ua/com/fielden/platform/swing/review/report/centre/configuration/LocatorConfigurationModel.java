package ua.com.fielden.platform.swing.review.report.centre.configuration;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.ILocatorManager;
import ua.com.fielden.platform.domaintree.ILocatorManager.Phase;
import ua.com.fielden.platform.domaintree.ILocatorManager.Type;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeRepresentation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.ei.development.EntityInspectorModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.centre.EntityLocatorModel;
import ua.com.fielden.platform.swing.review.report.centre.binder.CentrePropertyBinder;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;

public class LocatorConfigurationModel<T extends AbstractEntity<?>, R extends AbstractEntity<?>> extends AbstractCentreConfigurationModel<T, ILocatorDomainTreeManagerAndEnhancer> {

    /**
     * The class where the property specified with propertyName was declared.
     */
    private final Class<R> rootType;
    /**
     * The associated {@link ILocatorManager} instance.
     */
    private final ILocatorManager locatorManager;
    /**
     * Holds the selected entities in the locator.
     */
    private final List<T> locatorSelectionModel = new ArrayList<T>();

    /**
     * Initiates this {@link LocatorConfigurationModel} with instance of {@link IGlobalDomainTreeRepresentation}, entity type and {@link EntityFactory}.
     *
     * @param entityType - The entity type for which this {@link CentreConfigurationModel} will be created.
     * @param rootType - The entity type where the property specified with property name was declared.
     * @param gdtr - Associated {@link GlobalDomainTreeRepresentation} instance.
     * @param entityFactory - {@link EntityFactory} needed for wizard model creation.
     */
    public LocatorConfigurationModel( final Class<T> entityType, final Class<R> rootType, final String propertyName, final ILocatorManager locatorManager, final EntityFactory entityFactory, final ICriteriaGenerator criteriaGenerator){
	super(entityType, propertyName, entityFactory, null, criteriaGenerator);
	this.rootType = rootType;
	this.locatorManager = locatorManager;
    }

    /**
     * Returns value that indicates whether this locator was changed or not.
     *
     * @return
     */
    public boolean isChanged(){
	return locatorManager.isChangedLocatorManager(getRootType(), getName());
    }

    /**
     * Returns value that indicates whether locator is in usage phase or not.
     *
     * @return
     */
    public boolean isInUsagePhase(){
	return Phase.USAGE_PHASE == locatorManager.phaseAndTypeOfLocatorManager(getRootType(), getName()).getKey();
    }

    /**
     * Returns value that indicates whether locator is in editing phase or not.
     *
     * @return
     */
    public boolean isInEdititngPhase(){
	return Phase.EDITING_PHASE == locatorManager.phaseAndTypeOfLocatorManager(getRootType(), getName()).getKey();
    }

    /**
     * Returns value that indicates whether locator is in freeze phase or not.
     *
     * @return
     */
    public boolean isInFreezedPhase(){
	return Phase.FREEZED_EDITING_PHASE== locatorManager.phaseAndTypeOfLocatorManager(getRootType(), getName()).getKey();
    }

    /**
     * Returns the type of the locator: LOCAL or GLOBAL.
     *
     * @return
     */
    public Type getType(){
	return locatorManager.phaseAndTypeOfLocatorManager(getRootType(), getName()).getValue();
    }

    /**
     * Saves this locator's configuration.
     */
    public void save(){
	locatorManager.acceptLocatorManager(getRootType(), getName());
    }

    /**
     * Saves this locator manager in to global space.
     */
    public void saveGlobally(){
	locatorManager.saveLocatorManagerGlobally(getRootType(), getName());
    }

    /**
     * Discards changes for the this entity locator.
     */
    public void discard(){
	locatorManager.discardLocatorManager(getRootType(), getName());
    }

    /**
     * Resets this locator manager to default one.
     */
    public void reset(){
	locatorManager.resetLocatorManagerToDefault(getRootType(), getName());
    }

    /**
     * Refreshes this locator.
     */
    public void refresh(){
	locatorManager.refreshLocatorManager(getRootType(), getName());
    }

    /**
     * Freezes this locator.
     */
    public void freeze(){
	locatorManager.freezeLocatorManager(getRootType(), getName());
    }

    /**
     * Returns the {@link ILocatorDomainTreeManagerAndEnhancer} associated with this locator configuration model.
     *
     * @return
     */
    public ILocatorDomainTreeManagerAndEnhancer getLocator(){
	return locatorManager.getLocatorManager(getRootType(), getName());
    }

    /**
     * Returns the {@link ILocatorManager} instance. That is used for managing this locator.
     *
     * @return
     */
    public ILocatorManager getLocatorManager() {
	return locatorManager;
    }

    /**
     * Returns entity type with which this type is associated.
     *
     * @return
     */
    public Class<R> getRootType() {
	return rootType;
    }

    public List<T> getLocatorSelectionModel() {
        return locatorSelectionModel;
    }

    public void resetLocatorSelectionModel() {
        locatorSelectionModel.clear();
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(isInUsagePhase()){
	    throw new IllegalStateException("The locator must be refreshed first before opening!");
	}
	if(ReportMode.REPORT.equals(mode)){
	    final ILocatorDomainTreeManager ldtm = getLocator();
	    if(ldtm == null){
		throw new IllegalStateException("The locator must have been initialised");
	    }
	    if(ldtm.getSecondTick().checkedProperties(getEntityType()).isEmpty()){
		return new Result(this, new CanNotSetModeException("Please choose properties to add to the result set!"));
	    }
	}
	return Result.successful(this);
    }

    @Override
    protected final EntityLocatorModel<T> createEntityCentreModel() {
	if(isInUsagePhase()){
	    throw new IllegalStateException("The locator must be refreshed!");
	}
	final ILocatorDomainTreeManagerAndEnhancer ldtme = getLocator();
	if(ldtme == null || ldtme.getSecondTick().checkedProperties(getEntityType()).isEmpty()){
	    throw new IllegalStateException("The locator manager is not specified correctly!");
	}
	return new EntityLocatorModel<T>(createInspectorModel(getCriteriaGenerator().generateLocatorQueryCriteria(getEntityType(), ldtme)), locatorSelectionModel, getName());
    }

    @Override
    protected final DomainTreeEditorModel<T> createDomainTreeEditorModel() {
	if(isInUsagePhase()){
	    throw new IllegalStateException("locator must be refreshed!");
	}
	final ILocatorDomainTreeManagerAndEnhancer ldtm = getLocator();
	if(ldtm == null){
	    throw new IllegalStateException("The locator manager can not be null!");
	}
	return new DomainTreeEditorModel<T>(getEntityFactory(), ldtm, getEntityType());
    }

    /**
     * Creates the {@link EntityInspectorModel} for the specified criteria
     *
     * @param criteria
     * @return
     */
    private EntityInspectorModel<EntityQueryCriteria<ILocatorDomainTreeManagerAndEnhancer, T, IEntityDao<T>>> createInspectorModel(final EntityQueryCriteria<ILocatorDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria) {
	return new EntityInspectorModel<EntityQueryCriteria<ILocatorDomainTreeManagerAndEnhancer, T, IEntityDao<T>>>(criteria,//
		CentrePropertyBinder.<T> createLocatorPropertyBinder());
    }
}
