package ua.com.fielden.platform.swing.review.report.centre.configuration;

import java.util.HashSet;
import java.util.Set;

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
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.centre.EntityCentreModel;
import ua.com.fielden.platform.swing.review.report.centre.binder.CentrePropertyBinder;
import ua.com.fielden.platform.swing.review.report.centre.factory.IAnalysisBuilder;
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
     * The type of menu item with which this centre configuration model is associated.
     */
    private final Class<? extends MiWithConfigurationSupport<T>> menuItemType;

    /**
     * The associated {@link GlobalDomainTreeManager} instance.
     */
    private final IGlobalDomainTreeManager gdtm;

    /**
     * The associated analysis builder.
     */
    private final IAnalysisBuilder<T> analysisBuilder;

    /**
     * Initiates this {@link CentreConfigurationModel} with instance of {@link IGlobalDomainTreeManager}, entity type and {@link EntityFactory}.
     *
     * @param entityType - the entity type for which this {@link CentreConfigurationModel} will be created.
     * @param gdtm - Associated {@link GlobalDomainTreeManager} instance.
     * @param entityFactory - {@link EntityFactory} needed for wizard model creation.
     */
    public CentreConfigurationModel(final Class<? extends MiWithConfigurationSupport<T>> menuItemType, final String name, final IAnalysisBuilder<T> analysisBuilder, final IGlobalDomainTreeManager gdtm, final EntityFactory entityFactory, final IEntityMasterManager masterManager, final ICriteriaGenerator criteriaGenerator){
	super(getEntityTypeForMenuItemClass(menuItemType), name, entityFactory, masterManager, criteriaGenerator);
	this.menuItemType = menuItemType;
	this.analysisBuilder = analysisBuilder;
	this.gdtm = gdtm;
    }

    /**
     * Saves this configuration.
     */
    public void save(){
	gdtm.saveEntityCentreManager(menuItemType, getName());
    }

    /**
     * Discards changes in the entity centre.
     */
    public void discard(){
	gdtm.discardEntityCentreManager(menuItemType, getName());
    }

    /**
     * Saves as this configuration.
     */
    public void saveAs(final String saveAsName){
	gdtm.saveAsEntityCentreManager(menuItemType, getName(), saveAsName);
    }

    /**
     * Removes this configuration.
     */
    public void remove(){
	gdtm.removeEntityCentreManager(menuItemType, getName());
    }

    /**
     * Returns the value that indicates whether this entity centre has changed or not.
     *
     * @return
     */
    public boolean isChanged(){
	return gdtm.isChangedEntityCentreManager(menuItemType, getName());
    }

    /**
     * Returns value that indicates whether this entity centre is freezed or not.
     *
     * @return
     */
    public boolean isFreezed(){
	return gdtm.isFreezedEntityCentreManager(menuItemType, getName());
    }

    /**
     * Freezes the associated entity centre model.
     */
    public void freez(){
	gdtm.freezeEntityCentreManager(menuItemType, getName());
    }

    /**
     * Returns the entity centre manager for this centre configuration model.
     *
     * @return
     */
    public ICentreDomainTreeManagerAndEnhancer getEntityCentreManager(){
	return gdtm.getEntityCentreManager(menuItemType, getName());
    }

    /**
     * Initialises the entity centre.
     */
    public void initEntityCentreManager(){
	gdtm.initEntityCentreManager(menuItemType, getName());
    }

    /**
     * Returns the list of non principle entity centre list.
     *
     * @return
     */
    public Set<String> getNonPrincipleEntityCentreList(){
	final Set<String> names = new HashSet<String>(gdtm.entityCentreNames(menuItemType));
	names.remove(null); // remove principle centre key (null), which is returned in case when principle entity centre is persisted
	return names;
    }

    @Override
    protected final EntityCentreModel<T> createEntityCentreModel() {
	final ICentreDomainTreeManagerAndEnhancer cdtme = getEntityCentreManager();
	if(cdtme == null || cdtme.getSecondTick().checkedProperties(getEntityType()).isEmpty()){
	    throw new IllegalStateException("The centre manager is not specified");
	}
	return new EntityCentreModel<T>(createInspectorModel(getCriteriaGenerator().generateCentreQueryCriteria(getEntityType(), cdtme)), analysisBuilder, getMasterManager(), getName());
    }

    @Override
    protected final DomainTreeEditorModel<T> createDomainTreeEditorModel() {
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

    /**
     * Returns the entity type for the specified menu item type.
     *
     * @param menuItemType
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T extends AbstractEntity<?>> Class<T> getEntityTypeForMenuItemClass(final Class<? extends MiWithConfigurationSupport<T>> menuItemType){
	final EntityType etAnnotation = menuItemType.getAnnotation(EntityType.class);
	if (etAnnotation == null || etAnnotation.value() == null) {
	    throw new IllegalArgumentException("The menu item type " + menuItemType.getSimpleName() + " has no 'EntityType' annotation, which is necessary to specify the root type of the centre.");
	}
	final Class<T> root = (Class<T>)etAnnotation.value();
	return root;
    }
}
