package ua.com.fielden.platform.web.resources.webui.exceptions;

public class MissingEntityType extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MissingEntityType(final String msg) {
        super(msg);
    }

    public MissingEntityType(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
