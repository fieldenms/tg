package ua.com.fielden.platform.web.ioc;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.ServerGlobalDomainTreeManager;
import ua.com.fielden.platform.web.app.AbstractWebApp;
import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.test.server.WebApplicationServerModule;
import ua.com.fielden.platform.web.test.server.WebGlobalDomainTreeManager;

import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.binder.AnnotatedBindingBuilder;

/**
 * This interface adds <code>WebApp</code>-related logic to its implementors, mainly application-specific <code>ApplicationServerModules</code> will be extended with its interface
 * (which are used for dao-tests, PopulateDb). See {@link WebApplicationServerModule} as an example.
 *
 * @author TG Team
 *
 */
public interface IBasicWebApplicationServerModule {

    /**
     * Binds all needed resources to enable {@link IWebApp} logic.
     *
     * @param webApp
     */
    default public void bindWebAppResources(final IWebApp webApp) {
        /////////////////////////////// application specific ////////////////////////////
        bindType(IServerGlobalDomainTreeManager.class).to(ServerGlobalDomainTreeManager.class).in(Scopes.SINGLETON);
        bindType(IGlobalDomainTreeManager.class).to(WebGlobalDomainTreeManager.class);

        // bind IWebApp instance with defined masters / centres and other DSL-defined configuration
        bindType(IWebApp.class).toInstance(webApp);
    }

    /**
     * Initialises {@link IWebApp}, that was bound previously.
     *
     * @param injector
     */
    default public void initWebApp(final Injector injector) {
        final AbstractWebApp webApp = (AbstractWebApp) injector.getInstance(IWebApp.class);
        webApp.setInjector(injector);

        // initialise IWebApp with its masters / centres
        webApp.initConfiguration();
    }

    <T> AnnotatedBindingBuilder<T> bindType(final Class<T> type);
}
