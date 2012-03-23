package ua.com.fielden.platform.swing.ei.editors.development;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for for binding entity properties to corresponding editors.
 * 
 * @author 01es
 * 
 */
public interface IPropertyBinder<T extends AbstractEntity> {
    /**
     * Should create new or re-bind existing property editors for entity properties. Returns a map where property editors are mapped to a corresponding property names.
     * 
     * @param entity
     * @return
     */
    Map<String, IPropertyEditor> bind(T entity);
}
