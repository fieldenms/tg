package ua.com.fielden.platform.swing.review.report.analysis.grid.configuration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;

public class GridConfigurationModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisConfigurationModel<T, CDTME> {

    public static final String gridAnalysisName = "Main details";

    public GridConfigurationModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria){
	super(criteria, gridAnalysisName);
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(ReportMode.WIZARD.equals(mode)){
	    return new Result(new UnsupportedOperationException("The WIZARD mode is not supported for this type of analysis"));
	}
	return Result.successful(this);
    }

    public final GridAnalysisModel<T, CDTME> createGridAnalysisModel() {
	return new GridAnalysisModel<T, CDTME>(getCriteria());
    }

}
