package ua.com.fielden.platform.eql.exceptions;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;

/**
 * A generic runtime exception indicating any EQL related erroneous situation during metadata generation stage.
 * 
 * @author TG Team
 *
 */
public class EqlMetadataGenerationException extends EqlException {
    private static final long serialVersionUID = 1L;

    public EqlMetadataGenerationException(final String msg) {
        super(msg);
    }

    public EqlMetadataGenerationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
   
}