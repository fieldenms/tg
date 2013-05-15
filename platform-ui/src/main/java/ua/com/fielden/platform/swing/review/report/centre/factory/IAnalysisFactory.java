package ua.com.fielden.platform.swing.review.report.centre.factory;

import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.details.customiser.IDetailsCustomiser;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IAnalysisViewCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IToolbarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

/**
 * Factory for analysis.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <A>
 */
public interface IAnalysisFactory<T extends AbstractEntity<?>, A extends AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ? extends IAbstractAnalysisDomainTreeManager, ?>> {

    /**
     * Creates an analysis configuration view.
     *
     * @param owner
     * @param criteria
     * @param name
     * @param progressLayer
     * @return
     */
    A createAnalysis(final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, //
	    final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, //
	    final String name, //
	    final Map<Object, DetailsFrame> detailsCache,//
	    final BlockingIndefiniteProgressLayer progressLayer);

    /**
     * Specifies the {@link IToolbarCustomiser} instance for this analysis factory.
     *
     * @param analysisCustomiser
     * @param returns self
     */
    IAnalysisFactory<T, A> setToolbarCustomiser(final IToolbarCustomiser<?> toolbarCustomiser);

    /**
     * Specifies the {@link IAnalysisViewCustomiser} instance for this analysis factory.
     *
     * @param analysisViewCustomiser
     * @return
     */
    IAnalysisFactory<T, A> setAnalysisViewCustomiser(final IAnalysisViewCustomiser<?> analysisViewCustomiser);

    /**
     * Specifies the {@link IAnalysisQueryCustomiser} instance for this analysis factory.
     *
     * @param queryCustomiser
     * @return
     */
    IAnalysisFactory<T, A> setQueryCustomiser(final IAnalysisQueryCustomiser<T, ?> queryCustomiser);

    /**
     * Specifies the {@link IDetailsCustomiser} instance for this analysis factory.
     *
     * @param detailsCustomiser
     * @return
     */
    IAnalysisFactory<T, A> setDetailsCustomiser(final IDetailsCustomiser detailsCustomiser);
}
