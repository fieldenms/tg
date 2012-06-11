package ua.com.fielden.platform.swing.review.report.centre.factory;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

/**
 * Factory for analysis.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <A>
 */
public interface IAnalysisFactory<T extends AbstractEntity<?>, A extends AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ? extends IAbstractAnalysisDomainTreeManagerAndEnhancer, ?, ?>> {

    /**
     * Creates an analysis configuration view.
     *
     * @param owner
     * @param criteria
     * @param name
     * @param progressLayer
     * @return
     */
    A createAnalysis(final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final String name, final BlockingIndefiniteProgressLayer progressLayer);
}
