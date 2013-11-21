package ua.com.fielden.platform.swing.review.report.analysis.association;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;

public class AssociationAnalysisReview<T extends AbstractEntity<?>> extends AbstractAnalysisReview<T, ICentreDomainTreeManagerAndEnhancer, IAbstractAnalysisDomainTreeManager> {

    private static final long serialVersionUID = 3508595248282014853L;

    public AssociationAnalysisReview(final AbstractAnalysisReviewModel<T, ICentreDomainTreeManagerAndEnhancer, IAbstractAnalysisDomainTreeManager> model, final AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, IAbstractAnalysisDomainTreeManager, ?> owner) {
	super(model, owner);
    }

}
