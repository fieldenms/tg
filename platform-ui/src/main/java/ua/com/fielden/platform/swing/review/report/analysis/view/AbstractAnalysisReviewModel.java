package ua.com.fielden.platform.swing.review.report.analysis.view;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReviewModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;

public abstract class AbstractAnalysisReviewModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer, ADTM extends IAbstractAnalysisDomainTreeManager, LDT> extends AbstractEntityReviewModel<T, CDTME> {

    private final ADTM adtm;

    private final PageHolder pageHolder;

    public AbstractAnalysisReviewModel(final AbstractAnalysisConfigurationModel<T, CDTME> configurationModel, final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria, final ADTM adtm, final PageHolder pageHolder) {
	super(configurationModel, criteria);
	this.adtm = adtm;
	this.pageHolder = pageHolder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractAnalysisConfigurationModel<T, CDTME> getConfigurationModel() {
	return (AbstractAnalysisConfigurationModel<T, CDTME>)super.getConfigurationModel();
    }

    //    /**
    //     * Determines whether this analysis report can be exported to the external file.
    //     *
    //     * @return
    //     */
    //    public abstract boolean canExport();
    //
    //    /**
    //     * Determines whether this analysis report supports page by page data review.
    //     *
    //     * @return
    //     */
    //    public abstract boolean isPaginationSupported();

    /**
     * Returns the associated {@link IAbstractAnalysisDomainTreeManager}.
     *
     * @return
     */
    public final ADTM adtm(){
	return adtm;
    }

    /**
     * Returns the {@link PageHolder} instance, that is associated with this analysis model.
     *
     * @return
     */
    public final PageHolder getPageHolder(){
	return pageHolder;
    }

    /**
     * Executes analysis query, and returns the result set of the query execution.
     *
     * @return
     */
    abstract protected LDT executeAnalysisQuery();

    /**
     * Determines whether this analysis can load data, and returns {@link Result} instance with exception, warning, or successful result.
     *
     * @return
     */
    abstract protected Result canLoadData();
}
