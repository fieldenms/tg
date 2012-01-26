package ua.com.fielden.platform.swing.review.report.analysis.view;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReviewModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

public abstract class AbstractAnalysisReviewModel<T extends AbstractEntity, ADTM extends IAbstractAnalysisDomainTreeManager, LDT> extends AbstractEntityReviewModel<T, ICentreDomainTreeManager> {

    private final ADTM adtm;

    private final PageHolder pageHolder;

    public AbstractAnalysisReviewModel(final EntityQueryCriteria<ICentreDomainTreeManager, T, IEntityDao<T>> criteria, final ADTM adtm, final PageHolder pageHolder) {
	super(criteria);
	this.adtm = adtm;
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
