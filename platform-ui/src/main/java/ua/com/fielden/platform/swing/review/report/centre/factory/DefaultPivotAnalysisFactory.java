package ua.com.fielden.platform.swing.review.report.centre.factory;

import java.util.Map;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.details.AnalysisDetailsData;
import ua.com.fielden.platform.swing.review.details.DefaultGroupingAnalysisDetails;
import ua.com.fielden.platform.swing.review.details.customiser.IDetailsCustomiser;
import ua.com.fielden.platform.swing.review.details.customiser.MapBasedDetailsCustomiser;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IAnalysisViewCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IToolbarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.pivot.configuration.PivotAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.pivot.configuration.PivotAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

public class DefaultPivotAnalysisFactory<T extends AbstractEntity<?>> implements IAnalysisFactory<T, PivotAnalysisConfigurationView<T>> {

    /**
     * Details customiser for chart analysis.
     */
    private IDetailsCustomiser detailsCustomiser;

    public DefaultPivotAnalysisFactory(//
    final EntityFactory entityFactory, //
            final ICriteriaGenerator criteriaGenerator, //
            final IEntityMasterManager masterManager) {
        detailsCustomiser = new MapBasedDetailsCustomiser()//
        .addDetails(AnalysisDetailsData.class, new DefaultGroupingAnalysisDetails<T>(entityFactory, criteriaGenerator, masterManager));
    }

    @Override
    public PivotAnalysisConfigurationView<T> createAnalysis(final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, //
            final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, //
            final String name, //
            final Map<Object, DetailsFrame> detailsCache,//
            final BlockingIndefiniteProgressLayer progressLayer) {
        final PivotAnalysisConfigurationModel<T> analysisModel = new PivotAnalysisConfigurationModel<T>(criteria, name);
        return new PivotAnalysisConfigurationView<T>(analysisModel, detailsCache, detailsCustomiser, owner, progressLayer);
    }

    @Override
    public DefaultPivotAnalysisFactory<T> setToolbarCustomiser(final IToolbarCustomiser<?> toolbarCustomiser) {
        throw new UnsupportedOperationException("The analysis tool bar customiser can not be set for pivot analysis factory.");
    }

    @Override
    public IAnalysisFactory<T, PivotAnalysisConfigurationView<T>> setQueryCustomiser(final IAnalysisQueryCustomiser<T, ?> queryCustomiser) {
        throw new UnsupportedOperationException("The analysis query customiser can not be set for pivot analysis factory.");
    }

    @Override
    public IAnalysisFactory<T, PivotAnalysisConfigurationView<T>> setDetailsCustomiser(final IDetailsCustomiser detailsCustomiser) {
        this.detailsCustomiser = detailsCustomiser;
        return this;
    }

    @Override
    public IAnalysisFactory<T, PivotAnalysisConfigurationView<T>> setAnalysisViewCustomiser(final IAnalysisViewCustomiser<?> analysisViewCustomiser) {
        throw new UnsupportedOperationException("The analysis view customiser can not be set for pivot analysis factory.");
    }
}
