package ua.com.fielden.platform.swing.review.report.analysis.pivot.configuration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.pivot.PivotAnalysisModel;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;

public class PivotAnalysisConfigurationModel<T extends AbstractEntity<?>> extends AbstractAnalysisConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> {

    public PivotAnalysisConfigurationModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final String name) {
	super(criteria, name);
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(ReportMode.REPORT.equals(mode)){
	    final ICentreDomainTreeManagerAndEnhancer cdtme = getCriteria().getCentreDomainTreeMangerAndEnhancer();
	    IPivotDomainTreeManagerAndEnhancer pdtme = (IPivotDomainTreeManagerAndEnhancer)cdtme.getAnalysisManager(getName());
	    if(pdtme == null){
		cdtme.initAnalysisManagerByDefault(getName(), AnalysisType.PIVOT);
		pdtme = (IPivotDomainTreeManagerAndEnhancer)cdtme.getAnalysisManager(getName());
	    }
	    if(pdtme==null){
		return new Result(this, new IllegalStateException("Simple analysis with " + getName() + " name can not be created!"));
	    }
	    if(pdtme.getFirstTick().checkedProperties(getCriteria().getEntityClass()).size() == 0 //
		    && pdtme.getSecondTick().checkedProperties(getCriteria().getEntityClass()).size() == 0){
		return new Result(this, new CanNotSetModeException("Please choose distribution or aggregation properties!"));
	    }
	}
	return Result.successful(this);
    }

    final PivotAnalysisModel<T> createPivotAnalysisModel() {
	final ICentreDomainTreeManagerAndEnhancer cdtme = getCriteria().getCentreDomainTreeMangerAndEnhancer();
	final IPivotDomainTreeManagerAndEnhancer pdtme = (IPivotDomainTreeManagerAndEnhancer)cdtme.getAnalysisManager(getName());
	return new PivotAnalysisModel<T>(this, getCriteria(), pdtme, getPageHolder());
    }

    final DomainTreeEditorModel<T> createDomainTreeEditorModel() {
	final ICentreDomainTreeManagerAndEnhancer cdtme = getCriteria().getCentreDomainTreeMangerAndEnhancer();
	final IPivotDomainTreeManagerAndEnhancer pdtme = (IPivotDomainTreeManagerAndEnhancer)cdtme.getAnalysisManager(getName());
	return new DomainTreeEditorModel<T>(getCriteria().getEntityFactory(), pdtme, getCriteria().getEntityClass());
    }

}
