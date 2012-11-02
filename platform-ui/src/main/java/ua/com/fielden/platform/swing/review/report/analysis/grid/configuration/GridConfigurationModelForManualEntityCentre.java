package ua.com.fielden.platform.swing.review.report.analysis.grid.configuration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModelForManualEntityCentre;

public class GridConfigurationModelForManualEntityCentre<T extends AbstractEntity<?>> extends GridConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> {

    public GridConfigurationModelForManualEntityCentre(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria) {
	super(criteria);
    }

    @Override
    public GridAnalysisModel<T, ICentreDomainTreeManagerAndEnhancer> createGridAnalysisModel() {
        return new GridAnalysisModelForManualEntityCentre<>(getCriteria());
    }
}
