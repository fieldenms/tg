package ua.com.fielden.platform.entity.proxy;

import static java.lang.String.format;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * This is a method handler intercepts calls to proxied entities, but at the same time it also acts as a mixin to the proxy itself by providing additional functionality
 * if a form of extra information (e.g. it captures the parent entity of the property whre the proxied intance serves as a value).
 * <p>
 * It is important to remember that there should be a separate instance of this handler for each proxy instance as it carries instance specific state!
 *
 * @author TG Team
 *
 */
public class EntityMethodHandler implements MethodHandler {

    private final ProxyMode mode;
    private final AbstractEntity<?> owner;
    private final String propertyName;
    private final Class<? extends AbstractEntity<?>> type;
    private AbstractEntity<?> proxy;
    private final IEntityDao<?> coForProxy;

    public EntityMethodHandler(
            final Class<? extends AbstractEntity<?>> type, 
            final IEntityDao<?> coForProxy,
            final AbstractEntity<?> owner,
            final String propertyName,
            final ProxyMode mode) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner entity is required.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Property type is required.");
        }
        if (mode == ProxyMode.LAZY && coForProxy == null) {
            throw new IllegalArgumentException("Companion object for proxy in LAZY mode is required.");
        }
        this.type = type;
        this.coForProxy = coForProxy;
        this.mode = mode;
        this.owner = owner;
        this.propertyName = propertyName;
    }

    @Override
    public Object invoke(final Object self, final Method thisMethod, final Method proceed, final Object[] args) throws Throwable {
        System.out.printf("Owner %s of type %s for entity %s of type %s executing method %s in mode %s\n", owner.getId(), owner.getType(), proxy.getId(), proxy.getType(), thisMethod.getName(), mode);
        
        if (getProxy() == null) {
            throw new IllegalStateException("This method handler should not be used as the instance it proxies is null (most likely is has been converted to a non-proxied entity).");
        }
        
        switch (mode) {
        case STRICT:
//            System.out.printf("--- STRICT Attempting to invoke method %s on proxy for type type %s; owner: %s; ownerId: %s; ownerClass: %s; isOwnerProxy: %s; selfId: %s; selfClass: %s; \n", 
//                    proceed.getName(), type.getName(), owner, owner.getClass(), owner.getId(), ProxyFactory.isProxyClass(owner.getClass()), ((AbstractEntity) self).getId(), self.getClass());
            throw new StrictProxyException(owner, self, format("Attempting to invoke method %s on proxy for type type %s.", proceed.getName(), type.getName()));
        case LAZY:
            // TODO Call to findById should be change to load -- yet to be introduced method
            final AbstractEntity<?> entity = coForProxy.findById(proxy.getId());
            // obtain the invoked method for just instantiated real entity
            final Method method = entity.getType().getMethod(thisMethod.getName(), thisMethod.getParameterTypes());
            // TODO It's not quite clear what should be done with meta-property original and previos values in this case...
            owner.setInitialising(true);
            owner.set(propertyName, entity);
            owner.setInitialising(false);
            setProxy(null);
            return method.invoke(entity, args);
        default:
            throw new IllegalArgumentException(format("Proxy mode %s is not recognized.", mode));
        }
    }

    public AbstractEntity<?> getProxy() {
        return proxy;
    }

    public void setProxy(final AbstractEntity<?> proxy) {
        this.proxy = proxy;
    }

}
