package ua.com.fielden.platform.web.action;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.error.Result.failure;
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
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.web.centre.CentreConfigShareAction;

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
            final Optional<String> configUuid = isEmpty(chosenProperty()) ? empty() : of(chosenProperty());
            if (configUuid.isPresent()) {
                final IEntityCentreConfig eccCompanion = co(EntityCentreConfig.class);
                final User user = userProvider.getUser();
                final Optional<EntityCentreConfig> freshConfigOpt = findConfigOptByUuid(eccCompanion.withDbVersion(centreConfigQueryFor(user, selectionCrit().miType, selectionCrit().device, FRESH_CENTRE_NAME)), configUuid.get(), eccCompanion);
                if (freshConfigOpt.isPresent()) {
                    final Optional<String> saveAsName = of(obtainTitleFrom(freshConfigOpt.get().getTitle(), deviceSpecific(FRESH_CENTRE_NAME, selectionCrit().device)));
                    if (isDefaultOrLinkOrInherited(saveAsName, selectionCrit())) {
                        entity.setErrorMsg(SAVE_MSG);
                    } else {
                        final Optional<EntityCentreConfig> savedConfigOpt = findConfigOptByUuid(eccCompanion.withDbVersion(centreConfigQueryFor(selectionCrit().miType, selectionCrit().device, SAVED_CENTRE_NAME)), configUuid.get(), eccCompanion);
                        if (!savedConfigOpt.isPresent()) {
                            // in case where there is no configuration creator then it was inherited from base / shared and original configuration was deleted;
                            // this type of configuration (inherited, no upstream) still can exist and act like own-save as, however it should not be used for sharing
                            entity.setErrorMsg(SAVE_MSG);
                        } else if (!areEqual(savedConfigOpt.get().getOwner(), user)) {
                            // the creator of configuration is not current user;
                            // it means that configuration was made not shared to this user by original creator, and it should not be used for sharing
                            entity.setErrorMsg(SAVE_MSG);
                        }
                    }
                } else {
                    throw failure(CONFIG_DOES_NOT_EXIST);
                }
            } else {
                entity.setErrorMsg(SAVE_MSG);
            }
        }
        return entity;
    }
    
}