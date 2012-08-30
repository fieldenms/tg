package ua.com.fielden.platform.swing.review.report.analysis.lifecycle.configuration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;

public class LifecycleAnalysisConfigurationModel<T extends AbstractEntity<?>> extends AbstractAnalysisConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> {

    public LifecycleAnalysisConfigurationModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final String name) {
	super(criteria, name);
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(ReportMode.REPORT.equals(mode)){
	    final IAnalysisDomainTreeManager adtme = (IAnalysisDomainTreeManager)getAnalysisManager();
	    if(adtme==null){
		return new Result(this, new IllegalStateException("Simple analysis with " + getName() + " name can not be created!"));
	    }
	    return Result.successful(this);
	} else if(ReportMode.WIZARD.equals(mode)){
	    return new Result(this, new IllegalArgumentException("The wizard mode for lifecycle analysis is not supported!"));
	}
	return Result.successful(this);

    }

//    final ChartAnalysisModel<T> createChartAnalysisModel() {
//	final ICentreDomainTreeManagerAndEnhancer cdtme = getCriteria().getCentreDomainTreeMangerAndEnhancer();
//	final IAnalysisDomainTreeManager adtme = (IAnalysisDomainTreeManager)cdtme.getAnalysisManager(getName());
//	return new ChartAnalysisModel<T>(getCriteria(), adtme, getPageHolder());
//    }
}
