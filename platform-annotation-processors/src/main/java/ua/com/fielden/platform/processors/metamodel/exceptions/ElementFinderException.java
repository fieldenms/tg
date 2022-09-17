package ua.com.fielden.platform.processors.metamodel.exceptions;

/**
 * A runtime exception time to report errors pertaining to finding elements with {@link ElementFinder}.
 *
 * @author TG Team
 *
 */
public class ElementFinderException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ElementFinderException(final String message) {
        super(message);
    }

}