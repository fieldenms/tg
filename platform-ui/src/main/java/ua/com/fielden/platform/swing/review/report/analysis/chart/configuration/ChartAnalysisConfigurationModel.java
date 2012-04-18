package ua.com.fielden.platform.swing.review.report.analysis.chart.configuration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ChartAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;

public class ChartAnalysisConfigurationModel<T extends AbstractEntity<?>> extends AbstractAnalysisConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> {

    public ChartAnalysisConfigurationModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final String name) {
	super(criteria, name);
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(ReportMode.REPORT.equals(mode)){
	    final IAnalysisDomainTreeManagerAndEnhancer adtme = (IAnalysisDomainTreeManagerAndEnhancer)getAnalysisManager();
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
	final IAnalysisDomainTreeManagerAndEnhancer adtme = (IAnalysisDomainTreeManagerAndEnhancer)cdtme.getAnalysisManager(getName());
	return new ChartAnalysisModel<T>(getCriteria(), adtme, getPageHolder());
    }

    final DomainTreeEditorModel<T> createDomainTreeEditorModel() {
	final ICentreDomainTreeManagerAndEnhancer cdtme = getCriteria().getCentreDomainTreeMangerAndEnhancer();
	final IAnalysisDomainTreeManagerAndEnhancer adtme = (IAnalysisDomainTreeManagerAndEnhancer)cdtme.getAnalysisManager(getName());
	return new DomainTreeEditorModel<T>(getCriteria().getEntityFactory(), adtme, getCriteria().getEntityClass());
    }

}
