package ua.com.fielden.platform.javafx.gis.gps;

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
public abstract class GpsGridConfigurationModel<T extends AbstractEntity> extends GridConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> {
    public GpsGridConfigurationModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, ICentreDomainTreeManagerAndEnhancer>> queryCustomiser) {
	super(criteria, queryCustomiser);
    }

    @Override
    public abstract GpsGridAnalysisModel<T> createGridAnalysisModel();

    // TODO
//    @Override
//    public AbstractMessageGridAnalysisModel<T> createGridAnalysisModel() {
//        return new AbstractMessageGridAnalysisModel<T>(getCriteria(), getQueryCustomiser());
//    }
}
