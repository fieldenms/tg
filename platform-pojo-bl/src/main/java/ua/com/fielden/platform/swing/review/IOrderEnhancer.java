package ua.com.fielden.platform.swing.review;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompleted;

/**
 * 
 * @author oleh
 * 
 */
public interface IOrderEnhancer {
    <E extends AbstractEntity> IQueryOrderedModel<E> enhanceWithOrdering(final ICompleted notOrderedQuery, final Class resultType);
}
