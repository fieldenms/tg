package ua.com.fielden.platform.web.action.pre;

import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

import static ua.com.fielden.platform.web.action.pre.ConfirmationPreAction.ConfirmationButtons.*;

/// Factory for [IPreAction]s.
///
/// @author TG Team
public interface PreActions {

    /// Creates [IPreAction] that opens a confirmation dialog with custom `msg` and buttons NO and YES.
    static IPreAction yesNo(final String msg) {
        return new ConfirmationPreAction(msg, NO, YES);
    }

    /// Creates [IPreAction] that opens a confirmation dialog with custom `msg` and buttons CANCEL and OK.
    static IPreAction okCancel(final String msg) {
        return new ConfirmationPreAction(msg, CANCEL, OK);
    }

    /// Creates [IPreAction] that allows further Entity Centre `Edit`ing for the next entity on successful `SAVE`.
    static IPreAction sequentialEdit() {
        return new SequentialEditPreAction();
    }

    /// Creates [IPreAction] for Entity Centre `Edit` actions that allows navigation to another entity without closing the dialog.
    ///
    /// @param navigationType type description to inform user what type of entity is currently opened and is navigating
    static IPreAction entityNavigation(final String navigationType) {
        return new EntityNavigationPreAction(navigationType);
    }

}
