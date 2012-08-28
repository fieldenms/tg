package ua.com.fielden.platform.swing.review.report.analysis.chart.configuration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ChartAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;

public class ChartAnalysisConfigurationModel<T extends AbstractEntity<?>> extends AbstractAnalysisConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> {
    /**
     * Indicates whether analysis represents special "sentinel" analysis.
     */
    private final boolean isSentinel;

    public ChartAnalysisConfigurationModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final String name, final boolean isSentinel) {
	super(criteria, name);
	this.isSentinel = isSentinel;
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(ReportMode.REPORT.equals(mode)){
	    final IAnalysisDomainTreeManager adtme = (IAnalysisDomainTreeManager)getAnalysisManager();
	    if(adtme==null){
		return new Result(this, new IllegalStateException("Simple analysis with " + getName() + " name can not be created!"));
	    }
	    if(adtme.getFirstTick().checkedProperties(getCriteria().getEntityClass()).size() == 0){
		return new Result(this, new CanNotSetModeException("Please choose distribution properties!"));
	    }
	}
	return Result.successful(this);
    }

    final ChartAnalysisModel<T> createChartAnalysisModel() {
	final ICentreDomainTreeManagerAndEnhancer cdtme = getCriteria().getCentreDomainTreeMangerAndEnhancer();
	final IAnalysisDomainTreeManager adtme = (IAnalysisDomainTreeManager)cdtme.getAnalysisManager(getName());
	return new ChartAnalysisModel<T>(getCriteria(), adtme, getPageHolder());
    }

    /**
     * Indicates whether analysis represents special "sentinel" analysis.
     *
     * @return
     */
    public boolean isSentinel() {
        return isSentinel;
    }
}
