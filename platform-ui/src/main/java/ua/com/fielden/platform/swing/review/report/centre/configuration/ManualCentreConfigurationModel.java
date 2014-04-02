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
import ua.com.fielden.platform.swing.review.report.centre.factory.IAnalysisBuilder;
import ua.com.fielden.platform.swing.review.report.centre.factory.IAnalysisFactory;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;

public class ManualCentreConfigurationModel<T extends AbstractEntity<?>> extends AbstractCentreConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> {

    private final IAnalysisBuilder<T> analysisBuilder;
    private final IAnalysisFactory<T, ?> analysisFactory;
    private ManualCentreConfigurationView<T> view;

    /**
     * The {@link ICentreDomainTreeManagerAndEnhancer} instance for this analysis details.
     */
    private final ICentreDomainTreeManagerAndEnhancer cdtme;

    public ManualCentreConfigurationModel(final Class<T> entityType, //
            final IAnalysisFactory<T, ?> analysisFactory,//
            final ICentreDomainTreeManagerAndEnhancer cdtme, //
            final IEntityMasterManager masterManager, //
            final ICriteriaGenerator criteriaGenerator) {
        super(entityType, null, null, masterManager, criteriaGenerator);
        this.cdtme = cdtme;
        this.analysisFactory = analysisFactory;
        this.analysisBuilder = new DefaultAnalysisBuilder<>(analysisFactory);
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
        if (ReportMode.WIZARD.equals(mode)) {
            return new Result(this, new IllegalArgumentException("The wizard mode can not be set for manual entity centre."));
        }
        return Result.successful(this);
    }

    @Override
    protected DomainTreeEditorModel<T> createDomainTreeEditorModel() {
        throw new UnsupportedOperationException("The manual centre can not be configured!");
    }

    @Override
    protected EntityCentreModel<T> createEntityCentreModel() {
        return new EntityCentreModel<T>(createInspectorModel(getCriteriaGenerator().generateCentreQueryCriteria(getEntityType(), cdtme)), analysisBuilder, getMasterManager(), getName());
    }

    /**
     * Returns the {@link ICentreDomainTreeManagerAndEnhancer} instance for this manual entity centre config. model
     * 
     * @return
     */
    public ICentreDomainTreeManagerAndEnhancer getCdtme() {
        return cdtme;
    }

    public IAnalysisFactory<T, ?> getAnalysisFactory() {
        return analysisFactory;
    }

    /**
     * Builds and executes query.
     */
    public void refresh() {
        if (view != null && view.getPreviousView() != null) {
            if (view.getPreviousView().getSingleAnalysis().getModel().getMode() == ReportMode.REPORT) {
                view.getPreviousView().getSingleAnalysis().getPreviousView().refresh();
            }
        }
    }

    public ManualCentreConfigurationView<T> getView() {
        return view;
    }

    public void setView(final ManualCentreConfigurationView<T> view) {
        this.view = view;
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
