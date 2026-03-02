package ua.com.fielden.platform.entity;

import java.util.Optional;

/// A contract that enables an entity to define custom leave behaviour for an entity master.
///
/// ### Compound master
///
/// For compound masters, custom leave behaviour is defined at the level of menu items.
/// This interface should be implemented by a [menu item][AbstractFunctionalEntityForCompoundMenuItem] entity type directly
/// or by extending [AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave].
///
/// The actual leave behaviour should be implemented in the `save` method of the companion object associated with the menu item entity.
/// The system will save a menu item entity when the menu item is opened and when it is left (see [LeaveReason]).
/// To differentiate between these cases, method [#leaveReason()] should be used.
///
/// ### Simple master
///
/// For simple masters, custom leave behaviour is not supported at present.
///
/// ### Implementation notes
///
/// If this interface is implemented directly, the entity type must declare the necessary properties, as documented
/// in the methods of this interface.
///
public interface ICustomisableCanLeave {

    /// Indicates whether the leave action that initiated the saving of the entity can be performed.
    ///
    /// This method must be defined as an accessor for property `canLeave: boolean`.
    ///
    /// If this method returns false, it is required that these methods also return a value:
    /// * [#getCannotLeaveReason()]
    /// * [#canLeaveOptions()]
    /// * [#getCloseInstructions()]
    ///
    boolean isCanLeave();

    /// A setter for property `canLeave: boolean`.
    ///
    /// Application business logic should use this setter to specify whether the leave action can be performed.
    ///
    ICustomisableCanLeave setCanLeave(boolean canLeave);

    /// Returns the reason the leave action cannot be performed.
    ///
    /// This method must be defined as an accessor for property `cannotLeaveReason: String`.
    ///
    String getCannotLeaveReason();

    /// A setter for property `cannotLeaveReason: String`.
    ///
    /// Application business logic should use this setter to specify the reason why the leave action cannot be performed.
    ///
    ICustomisableCanLeave setCannotLeaveReason(String cannotLeaveReason);

    /// Returns a configuration for a confirmation dialog that will displayed if the leave action cannot be performed.
    ///
    /// This method must be implemented as an extra accessor for property `canLeaveOptions: String` that must store
    /// the [name][CanLeaveOptions#name()] of the enum instance.
    /// This extra effort is required because the platform does not support enums as property types at present.
    ///
    /// @return An optional containing the options if confirmation is required.
    ///         Otherwise, an empty optional.
    ///
    Optional<CanLeaveOptions> canLeaveOptions();

    /// An extra convenient setter for property `canLeaveOptions: String`.
    ///
    /// @see #canLeaveOptions()
    ///
    ICustomisableCanLeave setCanLeaveOptions(CanLeaveOptions canLeaveOptions);

    /// Returns the reason the leave action was initiated.
    /// This method should be used in application business logic to define custom leave behaviour.
    /// The returned optional will be present iff a leave action was initiated.
    ///
    /// This method must be implemented as an extra accessor for property `leaveReason: String` that must store
    /// the [name][LeaveReason#name()] of the enum instance.
    /// This extra effort is required because the platform does not support enums as property types at present.
    ///
    /// The underlying property is read-only from the application's perspective.
    /// The platform handles its assignment when a leave action is initiated.
    ///
    /// @return An optional containing the leave reason if a leave action was initiated.
    ///         Otherwise, an empty optional.
    ///
    Optional<LeaveReason> leaveReason();

    /// Returns a message to be displayed after the user rejects leaving in the confirmation dialog.
    /// This message should explain what the user can do to leave without confirmation.
    ///
    /// This method must be defined as an accessor for property `closeInstructions: String`.
    ///
    String getCloseInstructions();

    /// A setter for property `closeInstructions: String`.
    ///
    /// @see #getCloseInstructions()
    ///
    ICustomisableCanLeave setCloseInstructions(String closeInstructions);

}
