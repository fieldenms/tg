package ua.com.fielden.platform.swing.review.report.analysis.query.customiser;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.report.query.generation.IReportQueryGenerator;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;

/**
 * {@link IAnalysisQueryCustomiser} that always returns the same given instance of {@link IReportQueryGenerator}.
 * 
 * @author TG Team
 * 
 * @param <T>
 * @param <CDTME>
 */
public class PlainQueryCustomiser<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> implements IAnalysisQueryCustomiser<T, GridAnalysisModel<T, CDTME>> {

    private final IReportQueryGenerator<T> queryGenerator;

    /**
     * Creates {@link PlainQueryCustomiser} with specified {@link IReportQueryGenerator}
     * 
     * @param queryGenerator
     */
    public PlainQueryCustomiser(final IReportQueryGenerator<T> queryGenerator) {
        this.queryGenerator = queryGenerator;
    }

    @Override
    public IReportQueryGenerator<T> getQueryGenerator(final GridAnalysisModel<T, CDTME> analysisModel) {
        return queryGenerator;
    }

}
