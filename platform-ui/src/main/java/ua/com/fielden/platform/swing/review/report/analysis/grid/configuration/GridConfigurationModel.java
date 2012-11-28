package ua.com.fielden.platform.swing.review.report.analysis.grid.configuration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.DefaultGridAnalysisQueryCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;

public class GridConfigurationModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisConfigurationModel<T, CDTME> {

    public static final String gridAnalysisName = "Main details";

    private final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, CDTME>> queryCustomiser;

    public static <T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> GridConfigurationModel<T, CDTME> createWithCustomQueryCustomiser(//
	    final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria, //
	    final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, CDTME>> queryCustomiser){
	return new GridConfigurationModel<>(criteria, queryCustomiser);
    }

    public static <T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> GridConfigurationModel<T, CDTME> createWithDefaultQueryCustomiser(//
	    final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria){
	return new GridConfigurationModel<>(criteria, null);
    }

    protected GridConfigurationModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria, final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, CDTME>> queryCustomiser){
	super(criteria, gridAnalysisName);
	this.queryCustomiser = queryCustomiser == null ? new DefaultGridAnalysisQueryCustomiser<T, CDTME>() : queryCustomiser;
    }

    @Override
    protected Result canSetMode(final ReportMode mode) {
	if(ReportMode.WIZARD.equals(mode)){
	    return new Result(new UnsupportedOperationException("The WIZARD mode is not supported for this type of analysis"));
	}
	return Result.successful(this);
    }

    public GridAnalysisModel<T, CDTME> createGridAnalysisModel() {
	return new GridAnalysisModel<T, CDTME>(getCriteria(), queryCustomiser);
    }

    public final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, CDTME>> getQueryCustomiser() {
	return queryCustomiser;
    }

}
