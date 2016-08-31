package ua.com.fielden.platform.web.layout.api.impl;

public class LayoutException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LayoutException(final String msg) {
        super(msg);
    }

    public LayoutException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
