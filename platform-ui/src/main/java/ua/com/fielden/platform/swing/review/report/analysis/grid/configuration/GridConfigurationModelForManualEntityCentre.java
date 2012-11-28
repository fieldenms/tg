package ua.com.fielden.platform.swing.review.report.analysis.grid.configuration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModelForManualEntityCentre;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.ManualGridAnalysisQueryCustomiser;

public class GridConfigurationModelForManualEntityCentre<T extends AbstractEntity<?>> extends GridConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> {

    public static <T extends AbstractEntity<?>> GridConfigurationModelForManualEntityCentre<T> createWithDefaultManualQueryCustomiser(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria){
	return new GridConfigurationModelForManualEntityCentre<>(criteria, null);
    }

    public static <T extends AbstractEntity<?>> GridConfigurationModelForManualEntityCentre<T> createWithCustomManualQueryCustomiser(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria,//
	    final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, ICentreDomainTreeManagerAndEnhancer>> queryCustomiser){
	return new GridConfigurationModelForManualEntityCentre<>(criteria, queryCustomiser);
    }

    protected GridConfigurationModelForManualEntityCentre(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria,//
	    final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, ICentreDomainTreeManagerAndEnhancer>> queryCustomiser) {
	super(criteria, queryCustomiser == null ? new ManualGridAnalysisQueryCustomiser<T>() : queryCustomiser);
    }

    @Override
    public GridAnalysisModel<T, ICentreDomainTreeManagerAndEnhancer> createGridAnalysisModel() {
        return new GridAnalysisModelForManualEntityCentre<>(getCriteria(), getQueryCustomiser());
    }
}
