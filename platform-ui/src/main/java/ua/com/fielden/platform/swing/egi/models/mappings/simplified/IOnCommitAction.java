/**
 *
 */
package ua.com.fielden.platform.swing.egi.models.mappings.simplified;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;

/**
 * Wrapper around {@link ua.com.fielden.platform.swing.components.bind.ComponentFactory.IOnCommitAction} interface, that provides implementing classes with additional information.<br>
 * In each method, entity - is the entity which component is bounded to, entityGridInspector - is {@link EntityGridInspector} related to that context
 * 
 * @author Yura
 * 
 * @param <T>
 */
public interface IOnCommitAction<T extends AbstractEntity> {

    public void postCommitAction(T entity, EntityGridInspector<T> entityGridInspector);

    public void postSuccessfulCommitAction(T entity, EntityGridInspector<T> entityGridInspector);

    public void postNotSuccessfulCommitAction(T entity, EntityGridInspector<T> entityGridInspector);

}
