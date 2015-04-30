package ua.com.fielden.platform.web.ioc;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.ServerGlobalDomainTreeManager;
import ua.com.fielden.platform.web.app.AbstractWebApp;
import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.test.server.WebGlobalDomainTreeManager;

import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.binder.AnnotatedBindingBuilder;

public interface IBasicWebApplicationServerModule {

    default public void bindWebAppResources(final IWebApp webApp) {
        /////////////////////////////// application specific ////////////////////////////
        bindType(IServerGlobalDomainTreeManager.class).to(ServerGlobalDomainTreeManager.class).in(Scopes.SINGLETON);
        bindType(IGlobalDomainTreeManager.class).to(WebGlobalDomainTreeManager.class);

        // bind IWebApp instance with defined masters / centres and other DSL-defined configuration
        bindType(IWebApp.class).toInstance(webApp);
    }

    default public void initWebApp(final Injector injector) {
        final IWebApp webApp = injector.getInstance(IWebApp.class);
        ((AbstractWebApp) webApp).setInjector(injector);
        // init web application with its masters / centres
        webApp.initConfiguration();
    }

    <T> AnnotatedBindingBuilder<T> bindType(final Class<T> type);
}
