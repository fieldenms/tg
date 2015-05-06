package ua.com.fielden.platform.web.test.server;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.CentreManagerConfigurator;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;

import com.google.inject.Inject;

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

    @Inject
    public WebGlobalDomainTreeManager(final ISerialiser serialiser, final ISerialiser0 serialiser0, final EntityFactory factory, final IUserProvider userProvider, final IMainMenuItemController mainMenuItemController, final IEntityCentreConfigController entityCentreConfigController, final IEntityCentreAnalysisConfig entityCentreAnalysisConfigController, final IEntityMasterConfigController entityMasterConfigController, final IEntityLocatorConfigController entityLocatorConfigController, final IWebUiConfig webApp) {
        super(serialiser, serialiser0, factory, userProvider, mainMenuItemController, entityCentreConfigController, entityCentreAnalysisConfigController, entityMasterConfigController, entityLocatorConfigController);

        this.webApp = webApp;
    }

    @Override
    protected ICentreDomainTreeManagerAndEnhancer createDefaultCentre(final CentreManagerConfigurator centreConfigurator, final Class<?> root, final Class<?> menuItemType) {
        final EntityCentre entityCentre = webApp.getCentres().get(menuItemType);
        if (entityCentre != null) {
            return entityCentre.getDefaultCentre();
        } else {
            return super.createDefaultCentre(centreConfigurator, root, menuItemType);
        }
    }
}
