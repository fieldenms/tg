package ua.com.fielden.platform.web.gis.gps;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;

/**
 * A {@link GridConfigurationView}'s model for Message.
 *
 * @author Developers
 */
public abstract class GpsGridConfigurationModel2<T extends AbstractEntity<?>> extends GridConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> {
    public GpsGridConfigurationModel2(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, ICentreDomainTreeManagerAndEnhancer>> queryCustomiser) {
        super(criteria, queryCustomiser);
    }

    @Override
    public abstract GpsGridAnalysisModel2<T> createGridAnalysisModel();
}
