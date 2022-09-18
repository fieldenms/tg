package ua.com.fielden.platform.processors.test_utils.exceptions;

/**
 * A generic exception to indicate errors in test case configurations. 
 *
 * @author TG Team
 *
 */
public class TestCaseConfigException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TestCaseConfigException(final String message) {
        super(message);
    }

    public TestCaseConfigException(final String message, final Throwable cause) {
        super(message, cause);
    }

}