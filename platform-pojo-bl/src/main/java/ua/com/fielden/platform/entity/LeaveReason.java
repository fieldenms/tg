package ua.com.fielden.platform.entity;

/// Represents the reasons why a user may leave the entity master view.
///
/// A user may leave the entity master view for one of the following reasons:
///
/// - Navigating to another menu item or view.
/// - Closing the dialog or view that contains the entity master.
///
/// Switching between entities of the same type is treated as closing the current entity master.
///
public enum LeaveReason {
    /// The entity master was closed. This includes switching between entities of the same type.
    ///
    CLOSED,
    /// The user navigated to a different menu item or view.
    ///
    NAVIGATED;
}
