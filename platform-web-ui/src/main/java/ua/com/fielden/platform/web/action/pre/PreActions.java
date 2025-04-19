package ua.com.fielden.platform.web.action.pre;

import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

import static ua.com.fielden.platform.web.action.pre.ConfirmationPreAction.ConfirmationButtons.*;

/// Factory for [IPreAction]s.
///
/// @author TG Team
public interface PreActions {

    /// A convenient factory method to produce a confirmation dialog with buttons NO and YES.
    static IPreAction yesNo(final String msg) {
        return new ConfirmationPreAction(msg, NO, YES);
    }

    /// A convenient factory method to produce a confirmation dialog with buttons CANCEL and OK.
    static IPreAction okCancel(final String msg) {
        return new ConfirmationPreAction(msg, CANCEL, OK);
    }

    /// Creates [SequentialEditPreAction].
    static IPreAction sequentialEdit() {
        return new SequentialEditPreAction();
    }

    /// Creates pre-action for action that allows to navigate to another entity without closing dialog, such action can work only on EGI.
    ///
    /// @param navigationType type description that is used to inform user what type of entity is currently opened and is navigating
    static IPreAction entityNavigation(final String navigationType) {
        return new EntityNavigationPreAction(navigationType);
    }

}
