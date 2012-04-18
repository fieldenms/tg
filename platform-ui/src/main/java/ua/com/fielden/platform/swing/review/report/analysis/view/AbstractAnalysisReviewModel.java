package ua.com.fielden.platform.swing.review.report.analysis.view;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReviewModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

public abstract class AbstractAnalysisReviewModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer, ADTME extends IAbstractAnalysisDomainTreeManagerAndEnhancer, LDT> extends AbstractEntityReviewModel<T, CDTME> {

    private final ADTME adtme;

    private final PageHolder pageHolder;

    public AbstractAnalysisReviewModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria, final ADTME adtme, final PageHolder pageHolder) {
	super(criteria);
	this.adtme = adtme;
	this.pageHolder = pageHolder;
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
    public final ADTME adtme(){
	return adtme;
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
