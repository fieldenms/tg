package ua.com.fielden.platform.swing.review.report.analysis.association;

import java.io.IOException;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;

public class AssociationAnalysisModel<T extends AbstractEntity<?>> extends AbstractAnalysisReviewModel<T, ICentreDomainTreeManagerAndEnhancer, IAbstractAnalysisDomainTreeManager> {

    public AssociationAnalysisModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria) {
	super(criteria, null);
	// TODO Auto-generated constructor stub
    }

    @Override
    protected Result executeAnalysisQuery() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected Result exportData(final String fileName) throws IOException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected String[] getExportFileExtensions() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected String getDefaultExportFileExtension() {
	// TODO Auto-generated method stub
	return null;
    }

}
