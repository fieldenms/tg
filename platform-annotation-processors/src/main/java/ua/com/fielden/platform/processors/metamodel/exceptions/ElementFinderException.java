package ua.com.fielden.platform.processors.metamodel.exceptions;

import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;

/**
 * A runtime exception time to report errors pertaining to finding elements with {@link ElementFinder}.
 *
 * @author TG Team
 *
 */
public class ElementFinderException extends MetaModelProcessorException {
    private static final long serialVersionUID = 1L;

    public ElementFinderException(final String message) {
        super(message);
    }

    public ElementFinderException(final String message, final Throwable cause) {
        super(message, cause);
    }

}