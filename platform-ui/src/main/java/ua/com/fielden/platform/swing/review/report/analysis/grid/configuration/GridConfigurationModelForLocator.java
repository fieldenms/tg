package ua.com.fielden.platform.swing.review.report.analysis.grid.configuration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModelForLocator;

public class GridConfigurationModelForLocator<T extends AbstractEntity<?>> extends GridConfigurationModel<T, ILocatorDomainTreeManagerAndEnhancer> {

    public GridConfigurationModelForLocator(final EntityQueryCriteria<ILocatorDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria) {
	super(criteria, null);
    }

    @Override
    public GridAnalysisModelForLocator<T> createGridAnalysisModel() {
        return new GridAnalysisModelForLocator<>(getCriteria(), getQueryCustomiser());
    }

}
