package ua.com.fielden.platform.entity.proxy;

import java.lang.reflect.Method;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A proxy interceptor that restricts invocation of the specified property getter and setter.
 * 
 * @author TG Team
 *
 */
public class ProxyPropertyInterceptor {

    public final Class<? extends AbstractEntity<?>> ownerType;
    public final String proxiedPropName;

    /**
     * Constructs interceptor for the specified property in the provided entity type. 
     * 
     * @param ownerType -- entity type who's property is being proxied
     * @param proxiedPropName -- the name of the proxied property
     */
    public ProxyPropertyInterceptor(final Class<? extends AbstractEntity<?>> ownerType, final String proxiedPropName) {
        this.ownerType = ownerType;
        this.proxiedPropName = proxiedPropName;
    }

    @RuntimeType
    public Object accessInterceptor(@AllArguments Object[] allArguments, @Origin Method method) throws Exception {
        throw new StrictProxyException(null, null, String.format("Invocation of method [%s] is restricted due to unfetched property [%s] in type [%s].", method.getName(), proxiedPropName, ownerType.getName()));
    }
}