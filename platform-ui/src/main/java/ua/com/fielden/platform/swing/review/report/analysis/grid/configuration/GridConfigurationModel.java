package ua.com.fielden.platform.swing.review.report.analysis.grid.configuration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;

public class GridConfigurationModel<T extends AbstractEntity, DTM extends ICentreDomainTreeManager> extends AbstractAnalysisConfigurationModel<T, DTM> {


    public GridConfigurationModel(final EntityQueryCriteria<DTM, T, IEntityDao<T>> criteria){
	super(criteria, null);
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	switch(mode){
	case REPORT : return Result.successful(this);
	case WIZARD : return new Result(new UnsupportedOperationException("The WIZARD mode is not supported for this type of analysis"));
	}
	return Result.successful(this);
    }

    public GridAnalysisModel<T, DTM> createGridAnalysisModel() {
	return new GridAnalysisModel<T, DTM>(this, getCriteria(), getPageHolder());
    }

}
