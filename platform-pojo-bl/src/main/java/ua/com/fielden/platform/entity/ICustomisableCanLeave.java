package ua.com.fielden.platform.entity;

/// A contract to be implemented by a functional entity.
///
/// This contract indicates that the `canLeave` method, although invoked on the
/// client side, is executed on the server. The corresponding business logic
/// must be implemented in the `save` method of the associated companion object.
///
public interface ICustomisableCanLeave {

    /// Indicates whether the master view for this entity can be exited.
    ///
    boolean isCanLeave();

    /// Provides the reason why the master view for this entity cannot be exited.
    ///
    String getCannotLeaveReason();

    /// Returns the set of buttons to be displayed in the confirmation dialog
    /// during the execution of the `canLeave` method.
    ///
    CanLeaveOptions getCanLeaveOptions();

}
