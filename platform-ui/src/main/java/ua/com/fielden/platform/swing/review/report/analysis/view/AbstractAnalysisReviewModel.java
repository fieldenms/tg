package ua.com.fielden.platform.swing.review.report.analysis.view;

import java.io.IOException;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReviewModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

public abstract class AbstractAnalysisReviewModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer, ADTME extends IAbstractAnalysisDomainTreeManager> extends AbstractEntityReviewModel<T, CDTME> {

    private final ADTME adtme;

    /**
     * The page holder for this analysis.
     */
    private final PageHolder pageHolder;

    private AbstractAnalysisReview<T, CDTME, ADTME> analysisView;

    public AbstractAnalysisReviewModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria, final ADTME adtme) {
        super(criteria);
        this.adtme = adtme;
        this.pageHolder = new PageHolder();
        this.analysisView = null;
    }

    /**
     * Set the analysis view for this model. Please note that one can set analysis view only once. Otherwise The {@link IllegalStateException} will be thrown.
     * 
     * @param analysisView
     */
    final void setAnalysisView(final AbstractAnalysisReview<T, CDTME, ADTME> analysisView) {
        if (this.analysisView != null) {
            throw new IllegalStateException("The analysis view can be set only once!");
        }
        this.analysisView = analysisView;
    }

    /**
     * Returns the associated {@link IAbstractAnalysisDomainTreeManager}.
     * 
     * @return
     */
    public final ADTME adtme() {
        return adtme;
    }

    /**
     * Returns the {@link PageHolder} instance, that is associated with this analysis model.
     * 
     * @return
     */
    public final PageHolder getPageHolder() {
        return pageHolder;
    }

    protected AbstractAnalysisReview<T, CDTME, ADTME> getAnalysisView() {
        return analysisView;
    }

    /**
     * Runs the last executed query.
     * 
     * @return
     */
    protected Result reExecuteAnalysisQuery() {
        throw new UnsupportedOperationException();
    }

    /**
     * Executes analysis query, and returns the result set of the query execution.
     * 
     * @return
     */
    abstract protected Result executeAnalysisQuery();

    /**
     * Exports data in to external file.
     */
    abstract protected Result exportData(String fileName) throws IOException;

    /**
     * Returns the array of available file extensions to export.
     * 
     * @return
     */
    abstract protected String[] getExportFileExtensions();

    /**
     * Returns default export file extension if user didn't specified one.
     * 
     * @return
     */
    abstract protected String getDefaultExportFileExtension();
}
