package ua.com.fielden.platform.entity;

import jakarta.annotation.Nullable;

import java.util.Optional;

/// Represents the reasons why a user may leave the entity master view.
///
/// A user may leave the entity master view for one of the following reasons:
///
/// - Navigating to another menu item or view.
/// - Closing the dialog or view that contains the entity master.
///
/// Navigating between entities on the entity centre is treated as closing the current entity master.
///
public enum LeaveReason {
    /// The entity master was closed. This includes switching between entities of the same type.
    ///
    CLOSED,
    /// The user navigated to a different menu item or view.
    ///
    NAVIGATED;

    public static Optional<LeaveReason> of(final @Nullable String name) {
        return name == null ? Optional.empty() : Optional.of(LeaveReason.valueOf(name));
    }

}
