package ua.com.fielden.platform.criteria.generator.impl;

/**
 * Runtime exceptional situation occuring in {@link CriteriaGenerator}.
 * 
 * @author TG Team
 *
 */
public class CriteriaGeneratorException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public CriteriaGeneratorException(final String msg) {
        super(msg);
    }
    
    public CriteriaGeneratorException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}