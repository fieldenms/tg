package ua.com.fielden.platform.swing.review.report.analysis.details.configuration;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.ei.development.EntityInspectorModel;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.details.factory.DefaultAnalysisBuilder;
import ua.com.fielden.platform.swing.review.report.centre.EntityCentreModel;
import ua.com.fielden.platform.swing.review.report.centre.binder.CentrePropertyBinder;
import ua.com.fielden.platform.swing.review.report.centre.configuration.AbstractCentreConfigurationModel;
import ua.com.fielden.platform.swing.review.report.centre.factory.IAnalysisBuilder;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;

public class AnalysisDetailsConfigurationModel<T extends AbstractEntity<?>> extends AbstractCentreConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> {

    /**
     * The associated analysis builder.
     */
    private final IAnalysisBuilder<T> analysisBuilder;

    /**
     * The {@link ICentreDomainTreeManagerAndEnhancer} instance for this analysis details.
     */
    private final ICentreDomainTreeManagerAndEnhancer cdtme;

    public AnalysisDetailsConfigurationModel(final Class<T> entityType, final String name, final EntityFactory entityFactory, final ICriteriaGenerator criteriaGenerator, final IEntityMasterManager masterManager, final ICentreDomainTreeManagerAndEnhancer cdtme) {
	super(entityType, name, entityFactory, masterManager, criteriaGenerator);
	this.analysisBuilder = new DefaultAnalysisBuilder<>();
	this.cdtme = cdtme;
    }

    @Override
    protected EntityCentreModel<T> createEntityCentreModel() {
	return new EntityCentreModel<T>(createInspectorModel(getCriteriaGenerator().generateCentreQueryCriteria(getEntityType(), cdtme)), analysisBuilder, getMasterManager(), getName());
    }

    @Override
    protected DomainTreeEditorModel<T> createDomainTreeEditorModel() {
	throw new UnsupportedOperationException("The analysis details can not be configured!");
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(ReportMode.WIZARD.equals(mode)){
	    return new Result(this, new IllegalArgumentException("The wizard mode can not be set for analysis details."));
	}
	return Result.successful(this);
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
