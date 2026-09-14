package ua.com.fielden.platform.entity;

import jakarta.annotation.Nullable;

import java.util.Optional;

/// Represents the buttons available in the confirmation dialog shown when the `canLeave` method of an entity master is invoked.
///
public enum CanLeaveOptions {
    /// This option applies when the underlying entity cannot be modified
    /// (e.g., a master with a center), but the business logic determines that
    /// leaving the master is premature and the user must complete certain actions
    /// before exiting. In this case, two buttons with specific roles are required:
    ///
    /// - `Yes` — indicates that the user wants to leave the entity master,
    ///           regardless of the reason returned by [ICustomisableCanLeave#cannotLeaveReason()].
    /// - `No`  — indicates that the user does not want to leave the entity master.
    ///
    YES_NO,

    /// This option applies when the underlying entity has unsaved changes.
    /// In this case, three buttons with specific roles are required:
    ///
    /// - `Yes`    — indicates that the user wants to leave the entity master and save any changes to the underlying entity.
    /// - `No`     — indicates that the user wants to leave the entity master without saving any changes to the underlying entity.
    /// - `Cancel` — indicates that the user does not want to leave the entity master in order to review the changes.
    ///
    YES_NO_CANCEL;

    public static Optional<CanLeaveOptions> of(final @Nullable String name) {
        return name == null ? Optional.empty() : Optional.of(CanLeaveOptions.valueOf(name));
    }

}
