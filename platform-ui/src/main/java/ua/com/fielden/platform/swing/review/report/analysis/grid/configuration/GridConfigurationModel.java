package ua.com.fielden.platform.swing.review.report.analysis.grid.configuration;

import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;

public class GridConfigurationModel<T extends AbstractEntity, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisConfigurationModel<T, CDTME> {


    public GridConfigurationModel(final EntityQueryCriteria<CDTME, T, IEntityDao2<T>> criteria){
	super(criteria, "Main details");
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	switch(mode){
	case REPORT : return Result.successful(this);
	case WIZARD : return new Result(new UnsupportedOperationException("The WIZARD mode is not supported for this type of analysis"));
	}
	return Result.successful(this);
    }

    final GridAnalysisModel<T, CDTME> createGridAnalysisModel() {
	return new GridAnalysisModel<T, CDTME>(this, getCriteria(), getPageHolder());
    }

}
