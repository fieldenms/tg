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
import ua.com.fielden.platform.swing.review.details.customiser.AnalysisDetailsCustomiser;
import ua.com.fielden.platform.swing.review.details.customiser.IDetailsCustomiser;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.chart.configuration.ChartAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.chart.configuration.ChartAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IToolbarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

public class DefaultChartAnalysisFactory<T extends AbstractEntity<?>> implements IAnalysisFactory<T, ChartAnalysisConfigurationView<T>> {

    /**
     * Details customiser for chart analysis.
     */
    private IDetailsCustomiser detailsCustomiser;

    public DefaultChartAnalysisFactory(//
	    final EntityFactory entityFactory, //
	    final ICriteriaGenerator criteriaGenerator, //
	    final IEntityMasterManager masterManager){
	detailsCustomiser = new AnalysisDetailsCustomiser<>(entityFactory, criteriaGenerator, masterManager);
    }

    @Override
    public ChartAnalysisConfigurationView<T> createAnalysis(//
	    final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, //
	    final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, //
	    final String name, //
	    final Map<Object, DetailsFrame> detailsCache,//
	    final BlockingIndefiniteProgressLayer progressLayer) {
	final ChartAnalysisConfigurationModel<T> analysisModel = new ChartAnalysisConfigurationModel<T>(criteria, name, false);
	return new ChartAnalysisConfigurationView<T>(analysisModel, detailsCache, detailsCustomiser, owner, progressLayer);
    }

    @Override
    public DefaultChartAnalysisFactory<T> setToolbarCustomiser(final IToolbarCustomiser<?> toolbarCustomiser) {
	throw new UnsupportedOperationException("The analysis tool bar customiser can not be set for chart analysis factory.");
    }

    @Override
    public IAnalysisFactory<T, ChartAnalysisConfigurationView<T>> setQueryCustomiser(final IAnalysisQueryCustomiser<T, ?> queryCustomiser) {
	throw new UnsupportedOperationException("The analysis query customiser can not be set for chart analysis factory.");
    }

    @Override
    public IAnalysisFactory<T, ChartAnalysisConfigurationView<T>> setDetailsCustomiser(final IDetailsCustomiser detailsCustomiser) {
	this.detailsCustomiser = detailsCustomiser;
	return this;
    }

}
