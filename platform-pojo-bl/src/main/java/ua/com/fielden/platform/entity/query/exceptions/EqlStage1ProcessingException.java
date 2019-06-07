package ua.com.fielden.platform.entity.query.exceptions;

/**
 * A generic runtime exception indicating any EQL related erroneous situation during stage 1 processing of EQL models.
 * 
 * @author TG Team
 *
 */
public class EqlStage1ProcessingException extends EqlException {

    private static final long serialVersionUID = 1L;

    public EqlStage1ProcessingException(final String msg) {
        super(msg);
    }
    
    public EqlStage1ProcessingException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
    
}
