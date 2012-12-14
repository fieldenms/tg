package ua.com.fielden.platform.swing.review.report.centre.factory;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModelForManualEntityCentre;

public class DefaultGridForManualEntityCentreFactory<T extends AbstractEntity<?>> extends DefaultGridAnalysisFactory<T> {

    @Override
    protected GridConfigurationModelForManualEntityCentre<T> createAnalysisModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria){
	return GridConfigurationModelForManualEntityCentre.createWithCustomManualQueryCustomiser(criteria, getQueryCustomiser());
    }
}
