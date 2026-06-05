package ua.com.fielden.platform.web.utils;

import ua.com.fielden.platform.error.Result;

/// A special kind of [Result] that represent a property conflict, where a change to a property's value by another user
/// has caused it to conflict with the value edited by the current user.
///
/// Stack trace is always disabled for [PropertyConflict] instances, as this exception has a very specific purpose in platform code,
/// and is not intended to be used in application business logic.
///
public class PropertyConflict extends Result {
    private static final long serialVersionUID = 1L;
    
    public PropertyConflict(final Object instance, final String reason) {
        super(instance, new Exception(reason), false);
    }
}
