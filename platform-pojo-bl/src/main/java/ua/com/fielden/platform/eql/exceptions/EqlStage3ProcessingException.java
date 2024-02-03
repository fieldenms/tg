package ua.com.fielden.platform.eql.exceptions;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;

/**
 * A generic runtime exception indicating any EQL related erroneous situation during stage 3 processing of EQL models.
 * 
 * @author TG Team
 *
 */
public class EqlStage3ProcessingException extends EqlException {

    private static final long serialVersionUID = 1L;

    public EqlStage3ProcessingException(final String msg) {
        super(msg);
    }
    
    public EqlStage3ProcessingException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
    
}
