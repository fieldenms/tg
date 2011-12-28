package ua.com.fielden.platform.swing.review.report.analysis.grid.configuration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;

public class GridConfigurationModel<T extends AbstractEntity> extends AbstractAnalysisConfigurationModel {

    private final EntityQueryCriteria<ICentreDomainTreeManager, T, IEntityDao<T>> criteria;

    public GridConfigurationModel(final EntityQueryCriteria<ICentreDomainTreeManager, T, IEntityDao<T>> criteria){
	this.criteria = criteria;
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	switch(mode){
	case REPORT : return Result.successful(this);
	case WIZARD : return new Result(new UnsupportedOperationException("The WIZARD mode is not supported for this type of analysis"));
	}
	return Result.successful(this);
    }

    public EntityQueryCriteria<ICentreDomainTreeManager, T, IEntityDao<T>> getCriteria() {
	return criteria;
    }

    public GridAnalysisModel<T> createGridAnalysisModel() {
	return new GridAnalysisModel<T>(getCriteria());
    }

}
