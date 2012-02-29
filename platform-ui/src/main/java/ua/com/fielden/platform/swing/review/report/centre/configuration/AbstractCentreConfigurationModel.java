package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentreModel;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationModel;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;

public abstract class AbstractCentreConfigurationModel<T extends AbstractEntity, DTM extends ICentreDomainTreeManager> extends AbstractConfigurationModel {

    /**
     * The entity type for which this {@link AbstractCentreConfigurationModel} was created.
     */
    protected final Class<T> entityType;

    /**
     * The name (for entity centre) or property name (for entity locator) for which this centre/locator was created.
     */
    public final String name;

    /**
     * {@link EntityFactory}, needed for {@link DomainTreeEditorModel} creation.
     */
    protected final EntityFactory entityFactory;

    /**
     * {@link ICriteriaGenerator} instance needed for criteria generation.
     */
    protected final ICriteriaGenerator criteriaGenerator;

    /**
     * Initiates this {@link AbstractCentreConfigurationModel} with appropriate entity type, name/propertyName, entity factory and criteria generator.
     * 
     * @param entityType
     * @param name
     * @param entityFactory
     * @param criteriaGenerator
     */
    public AbstractCentreConfigurationModel(final Class<T> entityType, final String name, final EntityFactory entityFactory, final ICriteriaGenerator criteriaGenerator){
	this.entityType = entityType;
	this.name = name;
	this.entityFactory = entityFactory;
	this.criteriaGenerator = criteriaGenerator;
    }

    abstract protected AbstractEntityCentreModel<T, DTM> createEntityCentreModel();

    abstract protected DomainTreeEditorModel<T> createDomainTreeEditorModel();

}
