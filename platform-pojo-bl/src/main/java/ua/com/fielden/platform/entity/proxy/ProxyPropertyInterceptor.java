package ua.com.fielden.platform.entity.proxy;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import ua.com.fielden.platform.entity.Accessor;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

import java.lang.reflect.Method;

/// A proxy interceptor that restricts invocation of the specified property getter and setter.
///
public class ProxyPropertyInterceptor {

    public static final String ERR_UNFETCHED_PROPERTY = "Invocation of method [%s] is restricted due to unfetched property [%s] in type [%s].";

    private ProxyPropertyInterceptor() {}
    
    @RuntimeType
    public static Object accessInterceptor(@This Object self, @AllArguments Object[] allArguments, @Origin Method method) throws Exception {
        final String accessorName = method.getName();
        final String propName;
        if (Accessor.isAccessor(accessorName)) {
            propName = Accessor.deducePropertyNameFromAccessor(accessorName);
        } else {
            propName = Mutator.deducePropertyNameFromMutator(accessorName);
        }
        throw new StrictProxyException(ERR_UNFETCHED_PROPERTY.formatted(
                accessorName, 
                propName, 
                PropertyTypeDeterminator.stripIfNeeded(self.getClass()).getName()));
    }
}