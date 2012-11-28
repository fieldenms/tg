package ua.com.fielden.platform.swing.review.report.analysis.query.customiser;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.report.query.generation.GridAnalysisQueryGenerator;
import ua.com.fielden.platform.report.query.generation.IReportQueryGeneration;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;

public class DefaultGridAnalysisQueryCustomiser<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> implements IAnalysisQueryCustomiser<T, GridAnalysisModel<T, CDTME>> {

    @Override
    public IReportQueryGeneration<T> getQueryGenerator(final GridAnalysisModel<T, CDTME> analysisModel) {
	return new GridAnalysisQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer>(analysisModel.getCriteria().getEntityClass(), analysisModel.getCriteria().getCentreDomainTreeMangerAndEnhancer());
    }

}
