package ua.com.fielden.platform.web.utils;

import ua.com.fielden.platform.error.Result;

/**
 * A special kind of {@link Result} that represent property conflict, where another user has been changed the value and it is conflicting with currently edited value.
 * 
 * @author TG Team
 *
 */
public class PropertyConflict extends Result {
    private static final long serialVersionUID = 1L;
    
    public PropertyConflict(final Object instance, final String reason) {
        super(instance, new Exception(reason));
    }
}
