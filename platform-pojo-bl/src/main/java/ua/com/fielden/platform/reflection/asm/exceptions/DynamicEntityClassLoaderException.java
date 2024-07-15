package ua.com.fielden.platform.reflection.asm.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;

/**
 * Exception related to {@link DynamicEntityClassLoader}.
 * 
 * @author TG Team
 */
public class DynamicEntityClassLoaderException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public DynamicEntityClassLoaderException(final String msg) {
        super(msg);
    }

    public DynamicEntityClassLoaderException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}