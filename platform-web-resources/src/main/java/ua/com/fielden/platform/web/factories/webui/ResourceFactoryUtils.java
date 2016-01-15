package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.view.master.EntityMaster;

import com.google.inject.Injector;

/**
 * Contains a set of utilities for Web UI resource factories.
 *
 * @author TG Team
 *
 */
public class ResourceFactoryUtils {

    /**
     * Returns the user's name for this concrete thread (the user has been populated through the Web UI authentication mechanism -- see DefaultWebResourceGuard).
     *
     * @return
     */
    static String getUsername(final Injector injector) {
        return injector.getInstance(IUserProvider.class).getUser().getKey();
    }

    /**
     * Returns the global manager for the user for this concrete thread (the user has been populated through the Web UI authentication mechanism -- see DefaultWebResourceGuard).
     *
     * @return
     */
    static IGlobalDomainTreeManager getUserSpecificGlobalManager(final Injector injector) {
        return injector.getInstance(IServerGlobalDomainTreeManager.class).get(getUsername(injector));
    }

    /**
     * Returns the global manager for the user for this concrete thread (the user has been populated through the Web UI authentication mechanism -- see DefaultWebResourceGuard).
     *
     * @return
     */
    public static IGlobalDomainTreeManager getUserSpecificGlobalManager(final IServerGlobalDomainTreeManager serverGdtm, final IUserProvider userProvider) {
        return serverGdtm.get(userProvider.getUser().getKey());
    }

    /**
     * Finds the entity master using 'entityType' request attribute inside 'webUiConfig'.
     *
     * @param request
     * @param webUiConfig
     * @return
     */
    static EntityMaster<? extends AbstractEntity<?>> getEntityMaster(final Request request, final IWebUiConfig webUiConfig) {
        return getEntityMaster((String) request.getAttributes().get("entityType"), webUiConfig);
    }

    /**
     * Finds the entity master using 'entityTypeString' inside 'webUiConfig'.
     *
     * @param entityTypeString
     * @param webUiConfig
     * @return
     */
    public static EntityMaster<? extends AbstractEntity<?>> getEntityMaster(final String entityTypeString, final IWebUiConfig webUiConfig) {
        final Class<? extends AbstractEntity<?>> entityType = (Class<? extends AbstractEntity<?>>) ClassesRetriever.findClass(entityTypeString);
        return webUiConfig.getMasters().get(entityType);
    }

    /**
     * Finds the entity centre using 'miType' request attribute inside 'webUiConfig'.
     *
     * @param request
     * @param webUiConfig
     * @return
     */
    static EntityCentre<? extends AbstractEntity<?>> getEntityCentre(final Request request, final IWebUiConfig webUiConfig) {
        return getEntityCentre((String) request.getAttributes().get("mitype"), webUiConfig);
    }

    /**
     * Finds the entity centre using 'mitypeString' inside 'webUiConfig'.
     *
     * @param mitypeString
     * @param webUiConfig
     * @return
     */
    public static EntityCentre<? extends AbstractEntity<?>> getEntityCentre(final String mitypeString, final IWebUiConfig webUiConfig) {
        final Class<? extends MiWithConfigurationSupport<?>> miType = (Class<? extends MiWithConfigurationSupport<?>>) ClassesRetriever.findClass(mitypeString);
        return webUiConfig.getCentres().get(miType);
    }

    public static AbstractCustomView getCustomView(final String viewName, final IWebUiConfig webUiConfig) {
        return webUiConfig.getCustomViews().get(viewName);
    }
}
