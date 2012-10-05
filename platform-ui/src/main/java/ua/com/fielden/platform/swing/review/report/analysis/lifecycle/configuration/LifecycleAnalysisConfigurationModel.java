package ua.com.fielden.platform.swing.review.report.analysis.lifecycle.configuration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.lifecycle.LifecycleAnalysisModel;

public class LifecycleAnalysisConfigurationModel<T extends AbstractEntity<?>> extends AbstractAnalysisConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> {

    public LifecycleAnalysisConfigurationModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final String name) {
	super(criteria, name);
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(ReportMode.REPORT.equals(mode)){
	    final ILifecycleDomainTreeManager adtme = (ILifecycleDomainTreeManager)getAnalysisManager();
	    if(adtme==null){
		return new Result(this, new IllegalStateException("Simple analysis with " + getName() + " name can not be created!"));
	    }
	    final boolean hasDistributions = adtme.getFirstTick().checkedProperties(getCriteria().getEntityClass()).size() != 0;
	    final boolean hasCategories = adtme.getSecondTick().checkedProperties(getCriteria().getEntityClass()).size() != 0;
	    if(!hasDistributions || !hasCategories){
		return new Result(this, new CanNotSetModeException("Please choose " +
						(!hasDistributions ? "distribution" : "") +
						(!hasDistributions && !hasCategories ? " and " : "") +
						(!hasCategories ? "category" : "") + " properties!"));
	    }
	    return Result.successful(this);
	}
	return Result.successful(this);
    }

    public final LifecycleAnalysisModel<T> createChartAnalysisModel() {
	final ICentreDomainTreeManagerAndEnhancer cdtme = getCriteria().getCentreDomainTreeMangerAndEnhancer();
	final ILifecycleDomainTreeManager ldtme = (ILifecycleDomainTreeManager)cdtme.getAnalysisManager(getName());
	return new LifecycleAnalysisModel<T>(getCriteria(), ldtme);
    }
}
