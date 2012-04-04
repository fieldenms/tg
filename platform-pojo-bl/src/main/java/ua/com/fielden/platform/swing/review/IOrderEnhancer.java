package ua.com.fielden.platform.swing.review;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 *
 * @author oleh
 *
 */
public interface IOrderEnhancer {
    <E extends AbstractEntity<?>> EntityResultQueryModel<E> enhanceWithOrdering(final ICompleted notOrderedQuery, final Class resultType);
}
