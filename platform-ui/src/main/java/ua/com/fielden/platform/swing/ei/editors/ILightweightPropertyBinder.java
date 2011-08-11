package ua.com.fielden.platform.swing.ei.editors;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for for binding entity properties to corresponding editors with no state (implementation should not store editors inside).
 * 
 * @author 01es, Jhou
 * 
 */
public interface ILightweightPropertyBinder<T extends AbstractEntity> extends IPropertyBinder<T> {
    /**
     * Should re-bind existing property "editors" for entity properties.
     * 
     * @param entity
     * @return
     */
    void rebind(Map<String, IPropertyEditor> editors, T entity);
}
