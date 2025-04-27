package ua.com.fielden.platform.web.action;

import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.IAction;

/// Represents standard JS code to be used in centre with generators that support _modified data regeneration_ prompt.
///
/// Use it through [EntityCentre#injectCustomCodeOnAttach(JsCode)] passing `new ForceCentreRegenerationPostAction(...).build()` into it.
///
/// Corresponding generator should return `Result.failure(forceRegenerationExceptionMessage)`.
/// This is the case where the user tries to regenerate already modified data.
///
/// @author TG Team
public class GuardCentreRegenerationAction implements IAction<GuardCentreRegenerationAction> {
    private final String forceRegenerationExceptionMessage;
    private final String confirmationQuestion;

    /// Creates standard [GuardCentreRegenerationAction] with custom `confirmationQuestion` and `forceRegenerationExceptionMessage`.
    ///
    /// @param forceRegenerationExceptionMessage exception message while trying to regenerate already modified data;
    ///     this appears as toast
    /// @param confirmationQuestion this appears as dialog's message (to confirm or reject data regeneration);
    ///     it is provided with 'Yes' and 'No' buttons
    public GuardCentreRegenerationAction(final String forceRegenerationExceptionMessage, final String confirmationQuestion) {
        this.forceRegenerationExceptionMessage = forceRegenerationExceptionMessage;
        this.confirmationQuestion = confirmationQuestion;
    }

    @Override
    public JsCode build() {
        return new JsCode("""
            if (!self.old_postRun) {
                self.old_postRun = self._postRun;
                self._postRun = (function (criteriaEntity, newBindingEntity, result) {
                    self.old_postRun(criteriaEntity, newBindingEntity, result);
        
                    if (criteriaEntity !== null && !criteriaEntity.isValidWithoutException() && criteriaEntity.exceptionOccurred() !== null && criteriaEntity.exceptionOccurred().message === '%s') {
                        self.confirm('%s', [{name:'Yes', confirm:true, autofocus:true}, {name:'No'}]).then(function () {
                            return self.run(undefined, undefined, true); // forceRegeneration is true (isAutoRunning and isSortingAction are undefined)
                        }, function () {}); // skip legal rejection of promise (when 'No' button has been pressed)
                    }
                }).bind(self);
            }
        """.formatted(
            forceRegenerationExceptionMessage,
            confirmationQuestion
        ));
    }

    @Override
    public GuardCentreRegenerationAction andThen(GuardCentreRegenerationAction thatAction) {
        throw new UnsupportedOperationException("Method [andThen] is unsupported.");
    }

}
