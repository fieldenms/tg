/**
 *
 */
package ua.com.fielden.platform.swing.ei;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.ei.editors.IPropertyBinder;
import ua.com.fielden.platform.swing.review.CriteriaPropertyBinder;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;

/**
 * Model holding property editors of {@link EntityQueryCriteria} class
 *
 * @author TG Team
 *
 * @param <T>
 */
public class CriteriaInspectorModel<T extends AbstractEntity, DAO extends IEntityDao<T>, E extends EntityQueryCriteria<T, DAO>> extends EntityInspectorModel<E> {

    public CriteriaInspectorModel(final E entity, final IPropertyBinder<E> propertyBinder) {
	super(entity, propertyBinder);

    }

    /**
     * Creates property editors for visible properties
     *
     * @param entity
     */
    public CriteriaInspectorModel(final E entity) {
	super(entity, new CriteriaPropertyBinder<E>());

    }

}
