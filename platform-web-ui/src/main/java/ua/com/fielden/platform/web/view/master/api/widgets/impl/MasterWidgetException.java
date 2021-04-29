package ua.com.fielden.platform.web.view.master.api.widgets.impl;

/**
 * Runtime exception that should be thrown when illegal situations occur during master widget API usage.
 * 
 * @author TG Team
 *
 */
public class MasterWidgetException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public MasterWidgetException(final String msg) {
        super(msg);
    }
    
    public MasterWidgetException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}