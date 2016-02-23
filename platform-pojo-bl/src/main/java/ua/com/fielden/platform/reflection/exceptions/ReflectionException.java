package ua.com.fielden.platform.reflection.exceptions;

/**
 * A runtime exception to indicate TG specific reflection related exception.
 * 
 * @author TG Team
 *
 */
public class ReflectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public ReflectionException(final String msg) {
        super(msg);
    }

}
