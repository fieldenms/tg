package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.ei.development.EntityInspectorModel;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.centre.EntityCentreModel;
import ua.com.fielden.platform.swing.review.report.centre.binder.CentrePropertyBinder;
import ua.com.fielden.platform.swing.review.report.centre.factory.DefaultAnalysisBuilder;
import ua.com.fielden.platform.swing.review.report.centre.factory.DefaultGridForManualEntityCentreFactory;
import ua.com.fielden.platform.swing.review.report.centre.factory.IAnalysisBuilder;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;

public class ManualCentreConfigurationModel<T extends AbstractEntity<?>> extends AbstractCentreConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> {

    private final String bindedPropertyName;

    private final IAnalysisBuilder<T> analysisBuilder;

    /**
     * The {@link ICentreDomainTreeManagerAndEnhancer} instance for this analysis details.
     */
    private final ICentreDomainTreeManagerAndEnhancer cdtme;

    private AbstractEntity<?> bindedEntity;

    public ManualCentreConfigurationModel(final Class<T> entityType, //
	    final String name,//
	    final DefaultGridForManualEntityCentreFactory<T> analysisFactory,//
	    final ICentreDomainTreeManagerAndEnhancer cdtme, //
	    final IEntityMasterManager masterManager, //
	    final ICriteriaGenerator criteriaGenerator,//
	    final String bindedPropertyName) {
	super(entityType, name, null, masterManager, criteriaGenerator);
	this.cdtme = cdtme;
	this.analysisBuilder = new DefaultAnalysisBuilder<>(analysisFactory);
	this.bindedPropertyName = bindedPropertyName;
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(ReportMode.WIZARD.equals(mode)){
	    return new Result(this, new IllegalArgumentException("The wizard mode can not be set for manual entity centre."));
	}
	return Result.successful(this);
    }

    @Override
    protected DomainTreeEditorModel<T> createDomainTreeEditorModel() {
	throw new UnsupportedOperationException("The manual centre can not be configured!");
    }

    /**
     * Set the binding entity for this manual entity centre.
     *
     * @param masterEntity
     */
    public void setBindedEntity(final AbstractEntity<?> bindedEntity) {
	this.bindedEntity = bindedEntity;
    }

    /**
     * Returns the binding entity for this manual entity centre.
     *
     * @return
     */
    public AbstractEntity<?> getBindedEntity() {
	return bindedEntity;
    }

    /**
     * Returns the property name to which this manual entity centre was binded.
     *
     * @return
     */
    public String getBindedPropertyName() {
	return bindedPropertyName;
    }

    @Override
    protected EntityCentreModel<T> createEntityCentreModel() {
	return new EntityCentreModel<T>(createInspectorModel(getCriteriaGenerator().generateCentreQueryCriteria(getEntityType(), cdtme)), analysisBuilder, getMasterManager(), getName());
    }

    /**
     * Creates the {@link EntityInspectorModel} for the specified criteria
     *
     * @param criteria
     * @return
     */
    private EntityInspectorModel<EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>>> createInspectorModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria) {
	return new EntityInspectorModel<EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>>>(criteria,//
		CentrePropertyBinder.<T> createLocatorPropertyBinder());
    }
}
