package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentreModel;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationModel;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;

public abstract class AbstractCentreConfigurationModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractConfigurationModel {

    /**
     * The entity type for which this {@link AbstractCentreConfigurationModel} was created.
     */
    private final Class<T> entityType;

    /**
     * The name (for entity centre) or property name (for entity locator) for which this centre/locator was created.
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

    /**
     * {@link IEntityMasterManager} instance that incorporates entity masters in to entity centre.
     */
    private final IEntityMasterManager masterManager;

    /**
     * Initiates this {@link AbstractCentreConfigurationModel} with appropriate entity type, name/propertyName, entity factory and criteria generator.
     * 
     * @param entityType
     * @param name
     * @param entityFactory
     * @param criteriaGenerator
     */
    public AbstractCentreConfigurationModel(final Class<T> entityType, final String name, final EntityFactory entityFactory, final IEntityMasterManager masterManager, final ICriteriaGenerator criteriaGenerator) {
        this.entityType = entityType;
        this.name = name;
        this.entityFactory = entityFactory;
        this.criteriaGenerator = criteriaGenerator;
        this.masterManager = masterManager;
    }

    public String getName() {
        return name;
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public ICriteriaGenerator getCriteriaGenerator() {
        return criteriaGenerator;
    }

    protected EntityFactory getEntityFactory() {
        return entityFactory;
    }

    protected IEntityMasterManager getMasterManager() {
        return masterManager;
    }

    abstract protected AbstractEntityCentreModel<T, CDTME> createEntityCentreModel();

    abstract protected DomainTreeEditorModel<T> createDomainTreeEditorModel();

}
