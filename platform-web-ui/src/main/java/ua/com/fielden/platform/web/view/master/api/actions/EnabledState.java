package ua.com.fielden.platform.web.view.master.api.actions;

/**
 * A set of constants for declaring when an action should be enabled in relation to the state of an associated with that action Entity Master instance.
 *
 * @author TG Team
 *
 */
public enum EnabledState {
    /** Indicates that action should be enabled only when Entity Master is in <code>VIEW</code> state. */
    VIEW,
    /** Indicates that action should be enabled only when Entity Master is in <code>EDIT</code> state. */
    EDIT,
    /** Indicates that action should be enabled only when Entity Master is in any of its <code>VIEW</code> or <code>EDIT</code> state. */
    ANY
}
