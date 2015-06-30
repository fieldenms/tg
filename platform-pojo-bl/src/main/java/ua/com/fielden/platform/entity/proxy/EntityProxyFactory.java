package ua.com.fielden.platform.entity.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Finder;

/**
 * This factory should be used for creation of proxied entity instances of the specified entity type.
 *
 * @author TG Team
 *
 */
public class EntityProxyFactory<T extends AbstractEntity<?>> extends ProxyFactory {

    private final Set<String> skipMethods = new HashSet<>();
    private final Class<T> type;

    public EntityProxyFactory(final Class<T> type) {
        this.type = type;
        setSuperclass(type);
        
        this.skipMethods.add("getId");
        this.skipMethods.add("setId");
        this.skipMethods.add("getVersion");
        this.skipMethods.add("setVersion");
        this.skipMethods.add("getClass");
        
        super.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(final Method method) {
                return !skipMethods.contains(method.getName());
            }
        });
    }

    /**
     * A factory method for creating  proxy entities with a given <code>id</code> that is used as a property value of instance <code>owner</code>.
     * 
     * @param id -- persistent identifier of the entity instance being proxied 
     * @param owner -- an entity instance with proxied property
     * @param coForProxy -- a companion object for proxied entity, which get used only for lazy loading
     * @param mode -- specifies STRICT or LAZY proxy modes
     * @return
     */
    public T create(
            final Long id, 
            final AbstractEntity<?> owner, 
            final String propertyName,
            final IEntityDao<?> coForProxy, 
            final ProxyMode mode) {
        try {
            final EntityMethodHandler handler = new EntityMethodHandler(type, coForProxy, owner, propertyName, mode);
            @SuppressWarnings("unchecked")
            final T entityProxy = (T) create(new Class<?>[0], new Object[0], handler);
            final Field idField = Finder.getFieldByName(type, AbstractEntity.ID);
            final boolean accessible = idField.isAccessible();
            idField.setAccessible(true);
            idField.set(entityProxy, id);
            idField.setAccessible(accessible);
            handler.setProxy(entityProxy);
            return entityProxy;
        } catch (final Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void setFilter(final MethodFilter mf) {
        throw new IllegalStateException("Filter is set during factory instantiation and should not be modified.");
    }

}
