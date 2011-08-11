package ua.com.fielden.platform.swing.model;

/**
 * A state for UI models. The actual model and view implementation should take into account the current model state and use it to drive the model/view life cycle.
 * 
 * @author TG Team
 * 
 */
public enum UmState {
    EDIT, NEW, VIEW,
    /** Indicates that the model is in transition between states. */
    UNDEFINED,
    /** Should be used in conjunction with custom actions. Effect is similar to EDIT or NEW in a way that UI model is considered in unsaved mode. */
    CUSTOM;
}