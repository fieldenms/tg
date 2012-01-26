package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.centre.EntityCentreModel;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationModel;
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
public class CentreConfigurationModel<T extends AbstractEntity> extends AbstractConfigurationModel{

    /**
     * The associated {@link GlobalDomainTreeManager} instance.
     */
    private final IGlobalDomainTreeManager gdtm;

    /**
     * The entity type for which this {@link CentreConfigurationModel} was created.
     */
    private final Class<T> entityType;

    /**
     * The name of the entity centre if the name is equal null then this centre is principle otherwise it is not principle
     */
    private final String name;

    /**
     * {@link EntityFactory}, needed for {@link DomainTreeEditorModel} creation.
     */
    private final EntityFactory entityFactory;

    /**
     * {@link ICriteriaGenerator} instance needed for criteria generation.
     */
    private final ICriteriaGenerator criteriaGenerator;



    //    private IWizard previousWizard;
    //
    //    private IConfigurable previousReview;

    /**
     * Initiates this {@link CentreConfigurationModel} with instance of {@link IGlobalDomainTreeManager}, entity type and {@link EntityFactory}.
     * 
     * @param entityType - the entity type for which this {@link CentreConfigurationModel} will be created.
     * @param gdtm - Associated {@link GlobalDomainTreeManager} instance.
     * @param entityFactory - {@link EntityFactory} needed for wizard model creation.
     */
    public CentreConfigurationModel(final Class<T> entityType, final String name, final IGlobalDomainTreeManager gdtm, final EntityFactory entityFactory, final ICriteriaGenerator criteriaGenerator){
	this.entityType = entityType;
	this.name = name;
	this.gdtm = gdtm;
	this.entityFactory = entityFactory;
	this.criteriaGenerator = criteriaGenerator;
    }

    public EntityCentreModel<T> createEntityCentreModel() {
	final ICentreDomainTreeManager cdtm = gdtm.getEntityCentreManager(entityType(), name());
	if(cdtm == null){
	    throw new IllegalStateException("The centre manager is not specified");
	}
	return new EntityCentreModel<T>(criteriaGenerator.generateCentreQueryCriteria(entityType, cdtm), name());
    }

    public DomainTreeEditorModel<T> createDomainTreeEditorModel() {
	final ICentreDomainTreeManager cdtm = gdtm.getEntityCentreManager(entityType(), name());
	if(cdtm == null){
	    gdtm.initEntityCentreManager(entityType(), name());
	}
	return new DomainTreeEditorModel<T>(getEntityFactory(), gdtm.getEntityCentreManager(entityType(), name()), entityType());
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(ReportMode.REPORT.equals(mode)){
	    final ICentreDomainTreeManager cdtm = gdtm.getEntityCentreManager(entityType(), name());
	    if(cdtm == null){
		return new Result(this, new CanNotSetModeException("This report is opened for the first time!"));
	    }
	    if(cdtm.getSecondTick().checkedProperties(entityType()).isEmpty()){
		return new Result(this, new Exception("Please chose prpoerties to see in the table."));
	    }
	}
	return Result.successful(this);
    }

    private EntityFactory getEntityFactory() {
	return entityFactory;
    }

    final String name(){
	return name;
    }

    final IGlobalDomainTreeManager gdtm() {
	return gdtm;
    }

    final Class<T> entityType(){
	return entityType;
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
