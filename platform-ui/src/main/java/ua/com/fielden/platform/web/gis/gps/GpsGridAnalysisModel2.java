package ua.com.fielden.platform.web.gis.gps;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;

/**
 * A model for Message's {@link GridAnalysisView}.
 *
 * @author Developers
 */
public abstract class GpsGridAnalysisModel2<T extends AbstractEntity<?>> extends GridAnalysisModel<T, ICentreDomainTreeManagerAndEnhancer> {
    private boolean fitToBounds = false;
    private boolean firstQuery = false;
    private final Class<T> entityType;
    private final ISerialiser serialiser;

    public GpsGridAnalysisModel2(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, ICentreDomainTreeManagerAndEnhancer>> queryCustomiser, final Class<T> entityType) {
        super(criteria, queryCustomiser);
        this.entityType = entityType;
        this.serialiser = criteria.getSerialiser();
    }

    public boolean getFitToBounds() {
        return fitToBounds;
    }

    protected void setFitToBounds(final boolean fitToBounds) {
        this.fitToBounds = fitToBounds;
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public ISerialiser getSerialiser() {
        return serialiser;
    }

    public boolean isFirstQuery() {
        return firstQuery;
    }

    protected void setFirstQuery(final boolean firstQuery) {
        this.firstQuery = firstQuery;
    }
}
