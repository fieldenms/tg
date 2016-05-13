package ua.com.fielden.platform.web.app.config;

/**
 * Exception for {@link WebUiBuilder}.
 * 
 * @author TG Team
 *
 */
public class WebUiBuilderException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public WebUiBuilderException(final String message) {
        super(message);
    }
}
