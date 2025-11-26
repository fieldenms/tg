package ua.com.fielden.platform.web.action.pre;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;

/// A standard confirmation pre-action.
///
public class ConfirmationPreAction implements IPreAction {

    private final String message;
    private final List<String> buttons = new ArrayList<>();
    private final boolean withProgress;

    private enum ConfirmationButtons {

        YES("{name:'Yes', confirm:true, autofocus:true}"),
        NO("{name:'No'}"),
        OK("{name:'Ok', confirm:true, autofocus:true}"),
        CANCEL("{name:'Cancel'}");


        private final String code;
        private ConfirmationButtons(final String code) {
            this.code = code;
        }
    }

    private ConfirmationPreAction(final String message, final boolean withProgress, final ConfirmationButtons... buttons) {
        this.message = message;
        for (int buttonIndex = 0; buttonIndex < buttons.length; buttonIndex++) {
            this.buttons.add(buttons[buttonIndex].code);
        }
        this.withProgress = withProgress;
    }

    /// A convenient factory method to produce a confirmation dialog with buttons NO and YES.
    ///
    public static ConfirmationPreAction yesNo(final String msg) {
        return new ConfirmationPreAction(msg, false, ConfirmationButtons.NO, ConfirmationButtons.YES);
    }

    /// A convenient factory method to produce a confirmation dialog with buttons NO and YES and progress indication.
    ///
    /// The progress indicator (spinner) starts immediately after user confirmation.
    /// Also, the confirmation dialog stays up until the action is completed (either successfully or otherwise).
    /// This API is most useful with no-UI actions or actions with conditional `skipUi` property.
    ///
    public static ConfirmationPreAction yesNoWithProgress(final String msg) {
        return new ConfirmationPreAction(msg, true, ConfirmationButtons.NO, ConfirmationButtons.YES);
    }

    /// A convenient factory method to produce a confirmation dialog with buttons CANCEL and OK.
    ///
    public static ConfirmationPreAction okCancel(final String msg) {
        return new ConfirmationPreAction(msg, false, ConfirmationButtons.CANCEL, ConfirmationButtons.OK);
    }

    /// A convenient factory method to produce a confirmation dialog with buttons CANCEL and OK and progress indication.
    ///
    /// The progress indicator (spinner) starts immediately after user confirmation.
    /// Also, the confirmation dialog stays up until the action is completed (either successfully or otherwise).
    /// This API is most useful with no-UI actions or actions with conditional `skipUi` property.
    ///
    public static ConfirmationPreAction okCancelWithProgress(final String msg) {
        return new ConfirmationPreAction(msg, true, ConfirmationButtons.CANCEL, ConfirmationButtons.OK);
    }

    @Override
    public JsCode build() {
        return new JsCode(
            (withProgress ? """
                if (action) {
                    // In case where { withProgress: true } option was used, override action completion logic to close the dialog.
                    const isActionInProgressChanged = action.isActionInProgressChanged.bind(action);
                    action.isActionInProgressChanged = (function (newValue, oldValue) {
                        isActionInProgressChanged(newValue, oldValue);
                        // If action progress has been stopped.
                        if (newValue === false && oldValue === true) {
                            self.confirmationDialog(dialog => dialog.enableActions(action));
                        }
                    }).bind(action);
                    action.isActionSuccessfulChanged = (newValue, oldValue) => {
                        // If action has become successful.
                        if (newValue && !oldValue) {
                            self.confirmationDialog(dialog => dialog.close());
                        }
                    };
                }
            """ : "") + """
                return self.confirm('%s', [%s]%s);
            """.formatted(
                message.replace("'", "\\'"),
                join(buttons, ","),
                withProgress ? ", { withProgress: true }" : ""
            )
        );
    }

}
