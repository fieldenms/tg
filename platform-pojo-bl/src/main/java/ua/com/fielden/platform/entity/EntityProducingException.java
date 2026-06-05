package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// Runtime exceptional situation describing incorrect use of context decomposition API inside producer implementation.
///
public class EntityProducingException extends AbstractPlatformRuntimeException {

    public EntityProducingException(final String msg) {
        super(msg);
    }
    
    public EntityProducingException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}