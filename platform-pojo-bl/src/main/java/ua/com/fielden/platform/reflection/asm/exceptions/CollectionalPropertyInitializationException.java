package ua.com.fielden.platform.reflection.asm.exceptions;

import ua.com.fielden.platform.reflection.asm.impl.TypeMaker;

/**
 * Exception related to initialization of a collectional property during dynamic type generation/modification.
 *
 * @see {@link TypeMaker}
 * 
 * @author TG Team
 */
public class CollectionalPropertyInitializationException extends TypeMakerException {
    private static final long serialVersionUID = 1L;
    
    public CollectionalPropertyInitializationException(final String msg) {
        super(msg);
    }

    public CollectionalPropertyInitializationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}