package ua.com.fielden.platform.web.action.post;

import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

/**
 * Represents standard JS code to be used in centre with generators that support <i>modified data regeneration</i> prompt.
 * <p>
 * Use it through {@link EntityCentre#injectCustomCodeOnAttach(JsCode)} passing <code>new ForceCentreRegenerationPostAction(...).build()</code> into it.
 * <p>
 * Please note, that corresponding generator should return <code>Result.failure(forceRegenerationExceptionMessage)</code> (to be used with this standard post action)
 * when user tries to regenerate already modified data.
 * 
 * @author TG Team
 *
 */
public class GuardCentreRegenerationPostAction implements IPostAction {
    private final String forceRegenerationExceptionMessage;
    private final String confirmationQuestion;
    
    /**
     * Creates standard {@link GuardCentreRegenerationPostAction} with custom <code>confirmationQuestion</code> and <code>forceRegenerationExceptionMessage</code>.
     * 
     * @param forceRegenerationExceptionMessage -- exception message while trying to regenerate already modified data (this appears as toast)
     * @param confirmationQuestion -- this appears as dialog's message (to confirm or reject data regeneration) and is provided with 'Yes' and 'No' buttons
     */
    public GuardCentreRegenerationPostAction(final String forceRegenerationExceptionMessage, final String confirmationQuestion) {
        this.forceRegenerationExceptionMessage = forceRegenerationExceptionMessage;
        this.confirmationQuestion = confirmationQuestion;
    }

    @Override
    public JsCode build() {
        final JsCode jsCode = new JsCode(String.format(""
                + "const old_postRun = self._postRun;\n"
                + "self._postRun = (function (criteriaEntity, newBindingEntity, resultEntities, pageCount, renderingHints, summary, columnWidths, visibleColumnsWithOrder) {\n"
                + "    old_postRun(criteriaEntity, newBindingEntity, resultEntities, pageCount, renderingHints, summary, columnWidths, visibleColumnsWithOrder);\n"
                + "    \n"
                + "    if (criteriaEntity !== null && !criteriaEntity.isValidWithoutException() && criteriaEntity.exceptionOccured().message === '%s') {\n"
                + "        self.confirm('%s', [{name:'Yes', confirm:true, autofocus:true}, {name:'No'}]).then(function () {\n"
                + "            return self.run(undefined, true);\n" // forceRegeneration is true (isSortingAction undefined)
                + "        }, function () {});\n" // skip legal rejection of promise (when 'No' button has been pressed)
                + "    }\n"
                + "}).bind(self);\n",
                forceRegenerationExceptionMessage, confirmationQuestion));
        return jsCode;
    }

}
