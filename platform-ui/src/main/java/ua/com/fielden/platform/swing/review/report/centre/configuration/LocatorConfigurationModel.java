package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.ILocatorManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeRepresentation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.centre.EntityLocatorModel;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;

public class LocatorConfigurationModel<T extends AbstractEntity> extends AbstractCentreConfigurationModel<T, ILocatorDomainTreeManager> {

    /**
     * The associated {@link ILocatorManager} instance.
     */
    private final ILocatorManager locatorManager;

    /**
     * The entity type for which this {@link LocatorConfigurationModel} was created. This entity type will be used to retrieve global locator configuration.
     */
    private final Class<T> entityType;

    /**
     * The property name for which this locator is created.
     */
    private final String propertyName;

    /**
     * {@link EntityFactory}, needed for {@link DomainTreeEditorModel} creation.
     */
    private final EntityFactory entityFactory;

    /**
     * {@link ICriteriaGenerator} instance needed for criteria generation.
     */
    private final ICriteriaGenerator criteriaGenerator;

    /**
     * Initiates this {@link LocatorConfigurationModel} with instance of {@link IGlobalDomainTreeRepresentation}, entity type and {@link EntityFactory}.
     * 
     * @param entityType - the entity type for which this {@link CentreConfigurationModel} will be created.
     * @param gdtr - Associated {@link GlobalDomainTreeRepresentation} instance.
     * @param entityFactory - {@link EntityFactory} needed for wizard model creation.
     */
    public LocatorConfigurationModel(final Class<T> entityType, final String propertyName, final ILocatorManager locatorManager, final EntityFactory entityFactory, final ICriteriaGenerator criteriaGenerator){
	this.entityType = entityType;
	this.propertyName = propertyName;
	this.locatorManager = locatorManager;
	this.entityFactory = entityFactory;
	this.criteriaGenerator = criteriaGenerator;
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(ReportMode.REPORT.equals(mode)){
	    ILocatorDomainTreeManager ldtm = locatorManager.getLocatorManager(entityType(), propertyName());
	    if(ldtm == null){
		locatorManager.initLocatorManagerByDefault(entityType(), propertyName());
		ldtm = locatorManager.getLocatorManager(entityType(), propertyName());
	    }
	    if(ldtm == null){
		return new Result(this, new Exception("The locator manager must be initialised"));
	    }
	    if(ldtm.getSecondTick().checkedProperties(entityType()).isEmpty()){
		return new Result(this, new CanNotSetModeException("This report is opened for the first time!"));
	    }
	}
	return Result.successful(this);
    }

    @Override
    protected EntityLocatorModel<T> createEntityCentreModel() {
	final ILocatorDomainTreeManager ldtm = locatorManager.getLocatorManager(entityType(), propertyName());
	if(ldtm == null || ldtm.getSecondTick().checkedProperties(entityType()).isEmpty()){
	    throw new IllegalStateException("The locator manager is not specified correctly!");
	}
	return new EntityLocatorModel<T>(criteriaGenerator.generateLocatorQueryCriteria(entityType, ldtm), propertyName());
    }

    @Override
    protected DomainTreeEditorModel<T> createDomainTreeEditorModel() {
	final ILocatorDomainTreeManagerAndEnhancer ldtm = locatorManager.getLocatorManager(entityType(), propertyName());
	if(ldtm == null){
	    throw new IllegalStateException("The locator manager can not be null!");
	}
	return new DomainTreeEditorModel<T>(entityFactory(), ldtm, entityType());
    }

    private EntityFactory entityFactory() {
	return entityFactory;
    }

    final String propertyName(){
	return propertyName;
    }

    final ILocatorManager locatorManager() {
	return locatorManager;
    }

    final Class<T> entityType(){
	return entityType;
    }
}
