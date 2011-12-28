package ua.com.fielden.platform.swing.review.report.analysis.view;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReviewModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

public abstract class AbstractAnalysisReviewModel<T extends AbstractEntity, ADTM extends IAbstractAnalysisDomainTreeManager> extends AbstractEntityReviewModel<T, ICentreDomainTreeManager> {

    private final ADTM adtm;

    public AbstractAnalysisReviewModel(final EntityQueryCriteria<ICentreDomainTreeManager, T, IEntityDao<T>> criteria, final ADTM adtm) {
	super(criteria);
	this.adtm = adtm;
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
}
