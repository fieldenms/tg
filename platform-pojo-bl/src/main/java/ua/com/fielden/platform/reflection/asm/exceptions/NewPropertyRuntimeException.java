package ua.com.fielden.platform.reflection.asm.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;

/**
 * Runtime exception related to {@link NewProperty}.
 * 
 * @author TG Team
 */
public class NewPropertyRuntimeException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;
    
    public NewPropertyRuntimeException(final String msg) {
        super(msg);
    }

    public NewPropertyRuntimeException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
