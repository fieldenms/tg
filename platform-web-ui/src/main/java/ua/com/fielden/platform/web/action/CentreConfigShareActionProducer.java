package ua.com.fielden.platform.web.action;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.utils.EntityUtils.areEqual;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefaultOrLink;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefaultOrLinkOrInherited;
import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.SAVED_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.findConfigOptByUuid;

import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.centre.CentreConfigShareAction;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * A producer for new instances of entity {@link CentreConfigShareAction}.
 * <p>
 * Contains static functions for implementing custom share actions.
 *
 * @author TG Team
 *
 */
public class CentreConfigShareActionProducer extends DefaultEntityProducerWithContext<CentreConfigShareAction> {
    public static final String CONFIG_DOES_NOT_EXIST = "Configuration does not exist.";
    private static final String SAVE_MSG = "Please save and try again.";
    private static final String SAVE_OWN_COPY_MSG = "Only sharing of your own configurations is supported. Please save as your copy and try again.";
    private static final String DUPLICATE_SAVE_MSG = "Please duplicate, save and try again.";

    private final IUserProvider userProvider;

    @Inject
    public CentreConfigShareActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final IUserProvider userProvider) {
        super(factory, CentreConfigShareAction.class, companionFinder);
        this.userProvider = userProvider;
    }

    @Override
    protected CentreConfigShareAction provideDefaultValues(final CentreConfigShareAction entity) {
        if (contextNotEmpty()) {
            validateShareActionContext(selectionCrit(), co(EntityCentreConfig.class), userProvider)
                .ifFailure(failure -> entity.setErrorMessage(failure.getMessage()));
        }
        return entity;
    }

    /**
     * Validates share action context, i.e. returns failure {@link Result} in case where configuration can not be shared and successful {@link Result} otherwise.<br>
     * <p>
     * The rules are the following:<br>
     * 1. default config -- `Please save and try again.`
     * 2. non-existent config -- `Configuration does not exist.`
     * 3. loaded link config -- `Please save and try again.`
     * 4. inherited from shared / base config -- `Only sharing of your own configurations is supported. Please save as your copy and try again.`
     * 5. inherited from shared / base, deleted upstream, config -- `Please duplicate, save and try again.`
     * 6. inherited from shared, made unshared -- `Please duplicate, save and try again.`
     * <p>
     * IMPORTANT: at this stage computations are not supported for share actions.
     * 
     * @param contextDecomposer
     * @param eccCompanion
     * @param userProvider
     * @return
     */
    public static Result validateShareActionContext(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EntityCentreConfigCo eccCompanion, final IUserProvider userProvider) {
        final Optional<String> saveAsName = selectionCrit.saveAsName();
        if (!saveAsName.isPresent()) {
            return failure(SAVE_MSG); // default configuration (the one with empty saveAsName) can not be shared
        }
        final User user = userProvider.getUser();
        final Optional<String> configUuidOpt = selectionCrit.centreConfigUuid(saveAsName);
        if (!configUuidOpt.isPresent()) {
            return failure(CONFIG_DOES_NOT_EXIST); // all non-default configurations should have uuid; handle gracefully just in case
        }
        final String configUuid = configUuidOpt.get();
        final Class<? extends MiWithConfigurationSupport<?>> miType = selectionCrit.miType();
        final DeviceProfile device = selectionCrit.device();
        final Optional<EntityCentreConfig> freshConfigOpt = findConfigOptByUuid(configUuid, user, miType, device, FRESH_CENTRE_NAME, eccCompanion);
        if (!freshConfigOpt.isPresent()) {
            return failure(CONFIG_DOES_NOT_EXIST); // configuration does not exist and can not be shared
        }
        if (isDefaultOrLinkOrInherited(saveAsName, selectionCrit)) {
            if (isDefaultOrLink(saveAsName)) {
                return failure(SAVE_MSG); // [link] configuration can not be shared
            }
            return failure(SAVE_OWN_COPY_MSG); // [inherited from shared; inherited from base] configuration can not be shared
        }
        final Optional<EntityCentreConfig> savedConfigOpt = findConfigOptByUuid(configUuid, miType, device, SAVED_CENTRE_NAME, eccCompanion);
        if (!savedConfigOpt.isPresent() // in case where there is no configuration creator then it was inherited from base / shared and original configuration was deleted; this type of configuration (inherited, no upstream) still can exist and act like own-save as, however it should not be used for sharing
         || !areEqual(savedConfigOpt.get().getOwner(), user)) { // the creator of configuration is not current user; it means that configuration was made not shared to this user by original creator, and it should not be used for sharing
            return failure(DUPLICATE_SAVE_MSG);
        }
        return successful("Ok");
    }

    /**
     * Creates {@link IPreAction} for centre configuration sharing actions.
     * <p>
     * It makes entity centre's {@code _actionInProgress} property true if the action has started and false if it has completed.
     */
    public static IPreAction createPreAction() {
        return () -> new JsCode(
              "if (!action.oldIsActionInProgressChanged) {\n"
            + "    action.oldIsActionInProgressChanged = action.isActionInProgressChanged.bind(action);\n"
            + "    action.isActionInProgressChanged = (newValue, oldValue) => {\n"
            + "        action.oldIsActionInProgressChanged(newValue, oldValue);\n"
            + "        self._actionInProgress = newValue;\n" // enhance action's observer for isActionInProgress to set _actionInProgress to whole centre which controls disablement of all other buttons
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
            + "    if (toast.text === '" + SAVE_OWN_COPY_MSG + "') {\n"
            + "        toast.hasMore = true;\n"
            + "        toast.msgText = toast.text;\n"
            + "    } else {\n"
            + "        toast.hasMore = false;\n"
            + "        toast.msgText = '';\n"
            + "    }\n"
            + "    showNonCritical();\n"
            + "} else {\n"
            + "    navigator && navigator.clipboard && navigator.clipboard.writeText(link).then(() => {\n" // Writing into clipboard is always permitted for currently open tab (https://developer.mozilla.org/en-US/docs/Web/API/Clipboard/writeText) -- that's why promise error should never occur;
            + "        toast.text = 'Copied to clipboard.';\n" // if for some reason the promise will be rejected then 'Unexpected error occurred.' will be shown to the user and global handler will report that to the server.
            + "        toast.hasMore = true;\n"
            + "        toast.msgText = link;\n"
            + "        showNonCritical();\n"
            + "    });\n"
            + "}\n"
        );
    }

}