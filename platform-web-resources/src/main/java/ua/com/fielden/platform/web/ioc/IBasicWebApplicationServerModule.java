package ua.com.fielden.platform.web.ioc;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.ServerGlobalDomainTreeManager;
import ua.com.fielden.platform.web.app.AbstractWebApp;
import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.test.server.WebApplicationServerModule;
import ua.com.fielden.platform.web.test.server.WebGlobalDomainTreeManager;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.binder.AnnotatedBindingBuilder;

/**
 * This interface defines <code>Web UI</code> specific IoC binding contract,
 * which is to be implement by an application specific IoC module, used by an application server.
 * <p>
 *  Each concrete application is expected to have a principle IoC module <code>ApplicationServerModules</code> that binds everything except the <code>Web UI</code> related dependencies.
 *  The reason the principle IoC module cannot bind these dependencies is rooted in the fact that the're not visible at the <code>DAO</code> project module, where it must reside and be used for unit tests, data population and migration utilities and more.
 *  <p>
 *  Module {@link WebApplicationServerModule}, which governs <code>Web UI</code> dependencies for a platform demo and test application server, can be used as an example.
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
     * Initialises an already bound {@link IWebApp} instance.
     * The default implementation assumes that is has a concrete type {@link AbstractWebApp}.
     *
     * @param injector
     */
    default public void initWebApp(final Injector injector) {
        final AbstractWebApp webApp = (AbstractWebApp) injector.getInstance(IWebApp.class);
        webApp.setInjector(injector);

        // initialise IWebApp with its masters / centres
        webApp.initConfiguration();
    }

    /**
     * This method is provided purely for integration with an application specific IoC module.
     * Its implementation should delegate the call to method {@link Binder#bind(Class)} of that IoC module.
     *
     * @param type
     * @return
     */
    <T> AnnotatedBindingBuilder<T> bindType(final Class<T> type);
}
