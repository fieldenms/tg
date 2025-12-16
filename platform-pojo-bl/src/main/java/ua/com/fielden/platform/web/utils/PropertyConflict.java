package ua.com.fielden.platform.web.utils;

import ua.com.fielden.platform.error.Result;

/// A special kind of [Result] that represent a property conflict, where a change to a property's value by another user
/// has caused it to conflict with the value edited by the current user.
///
public class PropertyConflict extends Result {
    private static final long serialVersionUID = 1L;
    
    public PropertyConflict(final Object instance, final String reason) {
        // The created Exception will capture a stack trace, hence no need to do the same for Result.
        super(instance, new Exception(reason), false);
    }
}
