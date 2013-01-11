package ua.com.fielden.platform.swing.review.report.analysis.multipledec.configuration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IMultipleDecDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.multipledec.MultipleDecModel;

public class MultipleDecConfigurationModel<T extends AbstractEntity<?>> extends AbstractAnalysisConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> {

    public MultipleDecConfigurationModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final String name) {
	super(criteria, name);
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if (ReportMode.REPORT.equals(mode)) {
	    final IMultipleDecDomainTreeManager multipleDec = (IMultipleDecDomainTreeManager) getAnalysisManager();
	    if (multipleDec == null) {
		return new Result(this, new IllegalStateException("Multiple dec analysis with " + getName() + " name can not be created!"));
	    }
	    if (multipleDec.getFirstTick().checkedProperties(getCriteria().getEntityClass()).size() == 0 //
		    || multipleDec.getSecondTick().checkedProperties(getCriteria().getEntityClass()).size() == 0) {
		return new Result(this, new CanNotSetModeException("Please choose distribution or aggregation properties!"));
	    }
	}
	return Result.successful(this);
    }

    final MultipleDecModel<T> createMultipleDecModel() {
	final ICentreDomainTreeManagerAndEnhancer cdtme = getCriteria().getCentreDomainTreeMangerAndEnhancer();
	final IMultipleDecDomainTreeManager mddtme = (IMultipleDecDomainTreeManager) cdtme.getAnalysisManager(getName());
	return new MultipleDecModel<T>(getCriteria(), mddtme);
    }
}
