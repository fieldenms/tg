package ua.com.fielden.platform.entity.meta;

/**
 * Provides a contract for a logic that assigns meta-property properties based on the value of the entity property it is associated with. For example, {@link MetaProperty} has
 * property <code>editable</code> the value for which should be determined based on some custom logic that could use a value of the associated with this meta-property entity
 * property.
 * 
 * @author 01es
 * 
 */
public interface IMetaPropertyDefiner {
    /**
     * Should define values of meta-property properties based on the passed value of the entity property is corresponds to.
     * 
     * @param entityPropertyValue
     */
    void define(final MetaProperty property, final Object entityPropertyValue);
}
