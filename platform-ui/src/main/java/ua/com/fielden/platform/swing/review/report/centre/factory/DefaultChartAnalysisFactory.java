package ua.com.fielden.platform.swing.review.report.centre.factory;

import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.chart.configuration.ChartAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.chart.configuration.ChartAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

public class DefaultChartAnalysisFactory<T extends AbstractEntity<?>> implements IAnalysisFactory<T, ChartAnalysisConfigurationView<T>> {

    @Override
    public ChartAnalysisConfigurationView<T> createAnalysis(//
	    final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, //
	    final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, //
	    final String name, //
	    final Map<Object, DetailsFrame> detailsCache,//
	    final BlockingIndefiniteProgressLayer progressLayer) {
	final ChartAnalysisConfigurationModel<T> analysisModel = new ChartAnalysisConfigurationModel<T>(criteria, name, false);
	return new ChartAnalysisConfigurationView<T>(analysisModel, detailsCache, owner, progressLayer);
    }

}
