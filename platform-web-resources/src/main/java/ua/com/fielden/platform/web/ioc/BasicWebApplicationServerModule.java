package ua.com.fielden.platform.web.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.ServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.ioc.BasicWebServerModule;
import ua.com.fielden.platform.reflection.CompanionObjectAutobinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.impl.ThreadLocalUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.app.AbstractWebApp;
import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.test.server.WebGlobalDomainTreeManager;

import com.google.inject.Injector;
import com.google.inject.Scopes;

/**
 * Guice injector module for Web UI Testing Server.
 *
 * @author TG Team
 *
 */
public class BasicWebApplicationServerModule extends BasicWebServerModule {
    private final Class<? extends IUniversalConstants> universalConstantsType;
    private final List<Class<? extends AbstractEntity<?>>> domainTypes;
    private final IWebApp webApp;

    /**
     * The constructor with the largest number of arguments.
     *
     * @param defaultHibernateTypes
     * @param applicationEntityTypes
     * @param domainTypes
     * @param serialisationClassProviderType
     * @param automaticDataFilterType
     * @param universalConstantsType
     * @param props
     * @throws Exception
     */
    public BasicWebApplicationServerModule(//
    final Map<Class, Class> defaultHibernateTypes, //
            final IApplicationDomainProvider applicationDomainProvider,//
            final List<Class<? extends AbstractEntity<?>>> domainTypes,//
            final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
            final Class<? extends IFilter> automaticDataFilterType, //
            final Class<? extends IUniversalConstants> universalConstantsType,//
            final Properties props,
            final IWebApp webApp) throws Exception {
        super(defaultHibernateTypes, applicationDomainProvider, serialisationClassProviderType, automaticDataFilterType, null, props);
        this.universalConstantsType = universalConstantsType;
        this.domainTypes = domainTypes;
        this.webApp = webApp;
    }

    /**
     * An argument list reduced version of the above constructor, where <code>universalConstantsType</code> is specified as <code>null</code>.
     *
     * @param defaultHibernateTypes
     * @param applicationEntityTypes
     * @param domainTypes
     * @param serialisationClassProviderType
     * @param automaticDataFilterType
     * @param props
     * @throws Exception
     */
    public BasicWebApplicationServerModule(final Map<Class, Class> defaultHibernateTypes, //
            final IApplicationDomainProvider applicationDomainProvider,//
            final List<Class<? extends AbstractEntity<?>>> domainTypes,//
            final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
            final Class<? extends IFilter> automaticDataFilterType, //
            final Properties props,
            final IWebApp webApp) throws Exception {
        this(defaultHibernateTypes, applicationDomainProvider, domainTypes, serialisationClassProviderType, automaticDataFilterType, null, props, webApp);
    }

    @Override
    protected void configure() {
        super.configure();

        /////////////////////////////// application specific ////////////////////////////
        bind(IServerGlobalDomainTreeManager.class).to(ServerGlobalDomainTreeManager.class).in(Scopes.SINGLETON);
        bind(IGlobalDomainTreeManager.class).to(WebGlobalDomainTreeManager.class);

        // bind IUserProvider
        bind(IUserProvider.class).to(ThreadLocalUserProvider.class).in(Scopes.SINGLETON);

        if (universalConstantsType != null) {
            bind(IUniversalConstants.class).to(universalConstantsType).in(Scopes.SINGLETON);
        }

        // dynamically bind DAO implementations for all companion objects
        for (final Class<? extends AbstractEntity<?>> entityType : domainTypes) {
            CompanionObjectAutobinder.bindDao(entityType, binder());
        }

        // bind IWebApp instance with defined masters / centres and other DSL-defined configuration
        bind(IWebApp.class).toInstance(webApp);
    }

    @Override
    public void setInjector(final Injector injector) {
        super.setInjector(injector);

        ((AbstractWebApp) this.webApp).setInjector(injector);
    }

    public IWebApp webApp() {
        return webApp;
    }
}