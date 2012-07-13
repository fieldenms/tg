package ua.com.fielden.platform.swing.review.report.centre.factory;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

/**
 * A contract that allows one to build different analysis.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAnalysisBuilder<T extends AbstractEntity<?>> {

    /**
     * Creates an analysis configuration view.
     *
     * @param owner
     * @param criteria
     * @param name
     * @param progressLayer
     * @return
     */
    AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ? extends IAbstractAnalysisDomainTreeManager, ?, ?> createAnalysis(//
	    final AnalysisType analysisType, //
	    final String name, //
	    final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, //
	    final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, //
	    final BlockingIndefiniteProgressLayer progressLayer);
}
