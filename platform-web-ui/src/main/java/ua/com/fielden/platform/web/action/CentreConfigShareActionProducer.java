package ua.com.fielden.platform.web.action;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.utils.EntityUtils.areEqual;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefaultOrLinkOrInherited;
import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.SAVED_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.centreConfigQueryFor;
import static ua.com.fielden.platform.web.centre.CentreUpdater.deviceSpecific;
import static ua.com.fielden.platform.web.centre.CentreUpdater.obtainTitleFrom;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.findConfigOptByUuid;

import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.IContextDecomposer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.web.centre.CentreConfigShareAction;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * A producer for new instances of entity {@link CentreConfigShareAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigShareActionProducer extends DefaultEntityProducerWithContext<CentreConfigShareAction> {
    public static final String CONFIG_DOES_NOT_EXIST = "Configuration does not exist.";
    private static final String SAVE_MSG = "Please save and try again.";
    
    private final IUserProvider userProvider;
    
    @Inject
    public CentreConfigShareActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final IUserProvider userProvider) {
        super(factory, CentreConfigShareAction.class, companionFinder);
        this.userProvider = userProvider;
    }
    
    @Override
    protected CentreConfigShareAction provideDefaultValues(final CentreConfigShareAction entity) {
        if (contextNotEmpty()) {
            validateShareActionContext(this, co(EntityCentreConfig.class), userProvider)
                .ifFailure(failure -> entity.setErrorMessage(failure.getMessage()));
        }
        return entity;
    }
    
    /**
     * Validates share action context.
     * 
     * @param contextDecomposer
     * @param eccCompanion
     * @param userProvider
     * @return
     */
    public static Result validateShareActionContext(final IContextDecomposer contextDecomposer, final IEntityCentreConfig eccCompanion, final IUserProvider userProvider) {
        final Optional<String> configUuid = isEmpty(contextDecomposer.chosenProperty()) ? empty() : of(contextDecomposer.chosenProperty());
        if (configUuid.isPresent()) {
            final User user = userProvider.getUser();
            final Optional<EntityCentreConfig> freshConfigOpt = findConfigOptByUuid(eccCompanion.withDbVersion(centreConfigQueryFor(user, contextDecomposer.selectionCrit().miType, contextDecomposer.selectionCrit().device, FRESH_CENTRE_NAME)), configUuid.get(), eccCompanion);
            if (freshConfigOpt.isPresent()) {
                final Optional<String> saveAsName = of(obtainTitleFrom(freshConfigOpt.get().getTitle(), deviceSpecific(FRESH_CENTRE_NAME, contextDecomposer.selectionCrit().device)));
                if (isDefaultOrLinkOrInherited(saveAsName, contextDecomposer.selectionCrit())) {
                    return failure(SAVE_MSG);
                } else {
                    final Optional<EntityCentreConfig> savedConfigOpt = findConfigOptByUuid(eccCompanion.withDbVersion(centreConfigQueryFor(contextDecomposer.selectionCrit().miType, contextDecomposer.selectionCrit().device, SAVED_CENTRE_NAME)), configUuid.get(), eccCompanion);
                    if (!savedConfigOpt.isPresent()) {
                        // in case where there is no configuration creator then it was inherited from base / shared and original configuration was deleted;
                        // this type of configuration (inherited, no upstream) still can exist and act like own-save as, however it should not be used for sharing
                        return failure(SAVE_MSG);
                    } else if (!areEqual(savedConfigOpt.get().getOwner(), user)) {
                        // the creator of configuration is not current user;
                        // it means that configuration was made not shared to this user by original creator, and it should not be used for sharing
                        return failure(SAVE_MSG);
                    } else {
                        return successful("Ok");
                    }
                }
            } else {
                return failure(CONFIG_DOES_NOT_EXIST);
            }
        } else {
            return failure(SAVE_MSG);
        }
    }
    
    /**
     * Creates {@link IPreAction} for centre configuration sharing actions.
     * <p>
     * It promotes currently loaded 'configUuid' to chosenProperty (as part of action context);<br>
     *   and makes entity centre's _actionInProgress property true if the action has started and false if it has been completed.
     */
    public static IPreAction createPreAction() {
        return () -> new JsCode(
              "action.chosenProperty = self.configUuid;\n" // configUuid is present on entity centre the share actions are generated on
            + "if (!action.oldIsActionInProgressChanged) {\n"
            + "    action.oldIsActionInProgressChanged = action.isActionInProgressChanged.bind(action);\n"
            + "    action.isActionInProgressChanged = (newValue, oldValue) => {\n"
            + "        action.oldIsActionInProgressChanged(newValue, oldValue);\n"
            + "        self._actionInProgress = newValue;\n" // enhance action's observer for isActionInProgress to set _actionInProgress on whole centre disabling / enabling all other buttons
            + "    };\n"
            + "}\n"
        );
    }
    
    /**
     * Creates {@link IPostAction} for centre configuration sharing actions.
     * <p>
     * Shows non-intrusive informational message if centre configuration could not be shared, which is indicated by non-empty {@code errorMessageProperty}.<br>
     * Otherwise, copies URI into the clipboard and shows non-intrusive informational message about successful copying.
     */
    public static IPostAction createPostAction(final String errorMessageProperty) {
        final String valueCode = "functionalEntity.get('" + errorMessageProperty + "')";
        return () -> new JsCode(
              "const link = window.location.href;\n"
            + "const toast = master._toastGreeting();\n"
            + "const showNonCritical = () => {\n"
            + "    toast.showProgress = false;\n"
            + "    toast.isCritical = false;\n"
            + "    toast.show();\n"
            + "};\n"
            + "if (" + valueCode + ") {\n"
            + "    toast.text = " + valueCode + ";\n"
            + "    toast.hasMore = false;\n"
            + "    toast.msgText = '';\n"
            + "    showNonCritical();\n"
            + "} else {\n"
            + "    navigator.clipboard.writeText(link).then(() => {\n" // Writing into clipboard is always permitted for currently open tab (https://developer.mozilla.org/en-US/docs/Web/API/Clipboard/writeText) -- that's why promise error should never occur;
            + "        toast.text = 'Copied to clipboard.';\n" // if for some reason the promise will be rejected then 'Unexpected error occurred.' will be shown to the user and global handler will report that to the server.
            + "        toast.hasMore = true;\n"
            + "        toast.msgText = link;\n"
            + "        showNonCritical();\n"
            + "    });\n"
            + "}\n"
        );
    }
    
}