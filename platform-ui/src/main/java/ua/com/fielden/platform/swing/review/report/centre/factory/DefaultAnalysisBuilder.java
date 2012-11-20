package ua.com.fielden.platform.swing.review.report.centre.factory;

import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

public class DefaultAnalysisBuilder<T extends AbstractEntity<?>> implements IAnalysisBuilder<T> {

    private final IAnalysisFactory<T, ?> defaultAnalysisFactory;

    public DefaultAnalysisBuilder(){
	this(null);
    }

    public DefaultAnalysisBuilder(final IAnalysisFactory<T, ?> defaultAnalysisFactory){
	if (defaultAnalysisFactory == null) {
	    this.defaultAnalysisFactory = new DefaultGridAnalysisFactory<>();
	} else {
	    this.defaultAnalysisFactory = defaultAnalysisFactory;
	}
    }

    @Override
    public AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ? extends IAbstractAnalysisDomainTreeManager, ?> createAnalysis(//
	    final AnalysisType analysisType, //
	    final String name, //
	    final Map<Object, DetailsFrame> detailsCache, //
	    final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, //
	    final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, //
	    final BlockingIndefiniteProgressLayer progressLayer) {
	if(analysisType == null){
	    return defaultAnalysisFactory.createAnalysis(owner, criteria, name, detailsCache, progressLayer);
	}
	return null;
    }

}
