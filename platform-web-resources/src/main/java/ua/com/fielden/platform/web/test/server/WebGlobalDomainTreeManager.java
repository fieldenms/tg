package ua.com.fielden.platform.web.test.server;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfig;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;

/**
 * The web-application-centric implementation for user-specific server-side {@link IGlobalDomainTreeManager}.
 * <p>
 * Contains special handling for default centre configuration creation (based on Centre DSL).
 *
 * @author TG Team
 *
 */
public class WebGlobalDomainTreeManager extends GlobalDomainTreeManager implements IGlobalDomainTreeManager {
    private final IWebUiConfig webApp;
    private final IServerGlobalDomainTreeManager serverManager;
    
    @Inject
    public WebGlobalDomainTreeManager(final ISerialiser serialiser, final ISerialiser0 serialiser0, final EntityFactory factory, final IUserProvider userProvider, final IMainMenuItem mainMenuItemController, final IEntityCentreConfig entityCentreConfigController, final IEntityCentreAnalysisConfig entityCentreAnalysisConfigController, final IEntityMasterConfig entityMasterConfigController, final IEntityLocatorConfig entityLocatorConfigController, final IWebUiConfig webApp, final IServerGlobalDomainTreeManager serverManager, final IUser coUser) {
        super(serialiser, serialiser0, factory, userProvider, mainMenuItemController, entityCentreConfigController, entityCentreAnalysisConfigController, entityMasterConfigController, entityLocatorConfigController, coUser);
        
        this.webApp = webApp;
        this.serverManager = serverManager;
        this.avoidPersistentCentres();
    }
    
    @Override
    protected ICentreDomainTreeManagerAndEnhancer createDefaultCentre(final Class<?> root, final Class<?> menuItemType) {
        final EntityCentre entityCentre = webApp.getCentres().get(menuItemType);
        if (entityCentre != null) {
            return entityCentre.getDefaultCentre();
        } else {
            return super.createDefaultCentre(root, menuItemType);
        }
    }
    
    @Override
    public Optional<IGlobalDomainTreeManager> basedOnManager() {
        final User user = getUserProvider().getUser();
        return user.isBase() ? empty() : of(serverManager.get(user.getBasedOnUser().getId()));
    }
    
}