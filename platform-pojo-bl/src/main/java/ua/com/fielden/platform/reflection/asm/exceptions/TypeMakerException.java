package ua.com.fielden.platform.reflection.asm.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;
import ua.com.fielden.platform.reflection.asm.impl.TypeMaker;

/**
 * Runtime exception related to {@link TypeMaker}.
 * 
 * @author TG Team
 */
public class TypeMakerException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public TypeMakerException(final String msg) {
        super(msg);
    }

    public TypeMakerException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}