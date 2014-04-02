package ua.com.fielden.platform.swing.review.report.analysis.grid;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;
import ua.com.fielden.platform.swing.review.report.centre.configuration.ManualCentreWithLinkConfigurationModel;

public class GridAnalysisModelForManualEntityCentre<T extends AbstractEntity<?>, M extends AbstractEntity<?>> extends GridAnalysisModel<T, ICentreDomainTreeManagerAndEnhancer> {

    public GridAnalysisModelForManualEntityCentre(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, ICentreDomainTreeManagerAndEnhancer>> queryCustomiser) {
        super(criteria, queryCustomiser);
    }

    @SuppressWarnings("unchecked")
    public String getLinkProperty() {
        return ((ManualCentreWithLinkConfigurationModel<T, M>) getAnalysisView().getOwner().getOwner().getOwner().getModel()).getLinkProperty();
    }

    @SuppressWarnings("unchecked")
    public M getLinkEntity() {
        return ((ManualCentreWithLinkConfigurationModel<T, M>) getAnalysisView().getOwner().getOwner().getOwner().getModel()).getLinkPropertyValue();
    }
}
