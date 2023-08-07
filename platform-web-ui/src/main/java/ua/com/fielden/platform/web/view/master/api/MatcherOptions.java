package ua.com.fielden.platform.web.view.master.api;

/**
 * Enumeration that contains options for specifying custom value matcher in Entity Master autocompleters.
 */
public enum MatcherOptions {
    /**
     * Shows 'active only' toggle button in Entity Master autocompleter result dialogs for activatable properties.
     * May be useful for autocompleters with custom matcher, that already includes inactive values.
     */
    SHOW_ACTIVE_ONLY_ACTION;
}