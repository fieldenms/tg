package ua.com.fielden.platform.swing.review.report.analysis.query.customiser;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.report.query.generation.IReportQueryGenerator;
import ua.com.fielden.platform.report.query.generation.ManualGridAnalysisQueryGenerator;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModelForManualEntityCentre;

public class ManualGridAnalysisQueryCustomiser<T extends AbstractEntity<?>> implements IAnalysisQueryCustomiser<T, GridAnalysisModel<T, ICentreDomainTreeManagerAndEnhancer>> {

    @SuppressWarnings("unchecked")
    @Override
    public IReportQueryGenerator<T> getQueryGenerator(final GridAnalysisModel<T, ICentreDomainTreeManagerAndEnhancer> analysisModel) {
	if(!(analysisModel instanceof GridAnalysisModelForManualEntityCentre)){
	    throw new IllegalArgumentException("The analysis model must be of GridAnalysisModelForManualEntityCentre type");
	}
	final GridAnalysisModelForManualEntityCentre<T, ?> manualAnalysisModel = (GridAnalysisModelForManualEntityCentre<T, ?>)analysisModel;
	return new ManualGridAnalysisQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer>(//
		manualAnalysisModel.getCriteria().getEntityClass(),//
		manualAnalysisModel.getCriteria().getCentreDomainTreeMangerAndEnhancer(),//
		manualAnalysisModel.getLinkProperty(),//
		manualAnalysisModel.getLinkEntity());
    }

}
