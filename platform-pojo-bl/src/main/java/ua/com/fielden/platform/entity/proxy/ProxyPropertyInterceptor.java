package ua.com.fielden.platform.entity.proxy;

import static java.lang.String.format;

import java.lang.reflect.Method;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import ua.com.fielden.platform.entity.Accessor;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

/**
 * A proxy interceptor that restricts invocation of the specified property getter and setter.
 * 
 * @author TG Team
 *
 */
public class ProxyPropertyInterceptor {

    private ProxyPropertyInterceptor() {}
    
    @RuntimeType
    public static Object accessInterceptor(@AllArguments Object[] allArguments, @Origin Method method) throws Exception {
        final String accessorName = method.getName();
        final String propName;
        if (Accessor.isAccessor(accessorName)) {
            propName = Accessor.deducePropertyNameFromAccessor(accessorName);
        } else {
            propName = Mutator.deducePropertyNameFromMutator(accessorName);
        }
        throw new StrictProxyException(format("Invocation of method [%s] is restricted due to unfetched property [%s] in type [%s].", 
                accessorName, 
                propName, 
                PropertyTypeDeterminator.stripIfNeeded(method.getDeclaringClass()).getName()));
    }
}