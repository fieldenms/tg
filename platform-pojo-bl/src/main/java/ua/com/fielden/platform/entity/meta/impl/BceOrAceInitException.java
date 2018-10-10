package ua.com.fielden.platform.entity.meta.impl;

/**
 * A runtime exception that indicates erroneous situation pertaining to initialisation of ACE or BCE handlers. 
 * 
 * @author TG Team
 *
 */
public class BceOrAceInitException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public BceOrAceInitException(final String msg) {
        super(msg);
    }
    
    public BceOrAceInitException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
