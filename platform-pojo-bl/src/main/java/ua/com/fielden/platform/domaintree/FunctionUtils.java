package ua.com.fielden.platform.domaintree;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Some useful methods to work with {@link IFunction}.
 * 
 * @author TG Team
 * 
 */
public class FunctionUtils {

    public static void check(final Class<?> argumentType, final Set<Class<?>> applicableTypes) {
        final Class<?> normalised = EntityUtils.isEntityType(argumentType) ? AbstractEntity.class : (Number.class.isAssignableFrom(argumentType) ? Number.class : argumentType);
        if (!applicableTypes.contains(argumentType) && !applicableTypes.contains(normalised)) {
            throw new IllegalArgumentException("The type [" + argumentType + "] is not applicable to this function.");
        }
    }

    /**
     * Returns a set of applicable functions for concrete <code>argumentType</code>.
     * 
     * @param argumentType
     * @return
     */
    public static Set<Function> functionsFor(final Class<?> argumentType) {
        final Set<Function> functions = new LinkedHashSet<Function>();
        for (final Function function : EnumSet.allOf(Function.class)) {
            final Class<?> normalised = EntityUtils.isEntityType(argumentType) ? AbstractEntity.class : (Number.class.isAssignableFrom(argumentType) ? Number.class : argumentType);
            if (function.argumentTypes().contains(argumentType) || function.argumentTypes().contains(normalised)) { // argument type is applicable to function, so add:
                functions.add(function);
            }
        }
        if (functions.isEmpty()) {
            throw new IllegalArgumentException("Argument type [" + argumentType.getSimpleName() + "] is not applicable to any " + Function.class.getSimpleName()
                    + " function. Please do not use this type.");
        }
        return functions;
    }

}
