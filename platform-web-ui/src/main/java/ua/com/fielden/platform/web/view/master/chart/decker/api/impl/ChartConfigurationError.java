package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

/**
 * A runtime exception that indicates erroneous situation pertaining to an instance of {@link ChartDeck}.
 *
 * @author TG Team
 *
 */
public class ChartConfigurationError extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ChartConfigurationError(final String msg) {
        super(msg);
    }

    public ChartConfigurationError(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
