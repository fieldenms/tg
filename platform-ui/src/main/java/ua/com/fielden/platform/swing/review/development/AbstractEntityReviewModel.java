package ua.com.fielden.platform.swing.review.development;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;

public abstract class AbstractEntityReviewModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> {

    private final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria;

    public AbstractEntityReviewModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria) {
        this.criteria = criteria;
    }

    public EntityQueryCriteria<CDTME, T, IEntityDao<T>> getCriteria() {
        return criteria;
    }
}
