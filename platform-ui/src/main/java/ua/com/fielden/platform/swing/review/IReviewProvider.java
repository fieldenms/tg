package ua.com.fielden.platform.swing.review;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Contract for anything that provides {@link DynamicEntityReview} for the specified {@link DynamicEntityQueryCriteria}.
 *
 * @author oleh
 *
 * @param <T>
 * @param <DAO>
 * @param <R>
 */
public interface IReviewProvider<T extends AbstractEntity, DAO extends IEntityDao<T>> {

    /**
     * Returns the {@link DynamicEntityReview} for the specified {@link DynamicEntityQueryCriteria} instance.
     *
     * @param criteria
     * @return
     */
    public EntityReview<T, DAO, EntityQueryCriteria<T,DAO>> getEntityReview(EntityQueryCriteria<T, DAO> criteria);

}
