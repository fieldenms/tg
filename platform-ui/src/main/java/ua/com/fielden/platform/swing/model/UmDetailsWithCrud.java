package ua.com.fielden.platform.swing.model;

import java.util.Map;

import ua.com.fielden.platform.dao.IMasterDetailsDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyEditor;

/**
 * This is a UI model, which serves as the basis for implementing UI models to manage one-to-many and one-to-one associations representing aggregations. The implementation enforces
 * that this kind of model must be lazy.
 * 
 * @author TG Team
 * 
 * @param <M>
 * @param <D>
 * @param <C>
 */
public abstract class UmDetailsWithCrud<M extends AbstractEntity<?>, D extends AbstractEntity<?>, C extends IMasterDetailsDao<M, D>> extends UmDetails<M, D, C> {

    /**
     * Represents a current managed entity instance.
     */
    private D managedEntity;

    protected UmDetailsWithCrud(final M entity, final C companion, final ILightweightPropertyBinder<D> propertyBinder, final fetch<D> fm, final boolean lazy) {
        super(entity, companion, propertyBinder, fm, lazy);
    }

    /**
     * A factory method for creation of new entity instances.
     * <p>
     * This method should be implemented in descendant models since there is no possibility to provide a common implementation due to the nature of master/details relationship.
     * 
     * @return a new entity instance
     */
    protected abstract D newEntity(final EntityFactory factory);

    /**
     * There is always a case where there is no details association with a master. Thus, the only safe way to produce correct editors is to use a brand new instance of the details
     * entity.
     */
    @Override
    protected Map<String, IPropertyEditor> buildEditors(final M entity, final C companion, final ILightweightPropertyBinder<D> propertyBinder) {
        setManagedEntity(newEntity(entity.getEntityFactory()));
        return propertyBinder.bind(getManagedEntity());
    }

    protected void setManagedEntity(final D entity) {
        managedEntity = entity;
    }

    @Override
    protected D getManagedEntity() {
        return managedEntity;
    }
}
