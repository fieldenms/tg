package ua.com.fielden.platform.reflection.asm.exceptions;

import ua.com.fielden.platform.reflection.asm.api.NewProperty;

/**
 * Exception related to {@link NewProperty}.
 * 
 * @author TG Team
 */
public class NewPropertyException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public NewPropertyException() {
        super();
    }
    
    public NewPropertyException(final String msg) {
        super(msg);
    }

    public NewPropertyException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
