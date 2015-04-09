package ua.com.fielden.platform.web.test.server;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.ioc.BasicWebServerModule;
import ua.com.fielden.platform.reflection.CompanionObjectAutobinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.impl.ThreadLocalUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.app.IWebApp;

import com.google.inject.Injector;
import com.google.inject.Scopes;

/**
 * Guice injector module for Web UI Testing Server.
 *
 * @author TG Team
 *
 */
public class ApplicationServerModule extends BasicWebServerModule {
    private final Class<? extends IUniversalConstants> universalConstantsType;
    private final List<Class<? extends AbstractEntity<?>>> domainTypes;
    private final IWebApp webApp = new WebApp();

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
    public ApplicationServerModule(//
    final Map<Class, Class> defaultHibernateTypes, //
            final IApplicationDomainProvider applicationDomainProvider,//
            final List<Class<? extends AbstractEntity<?>>> domainTypes,//
            final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
            final Class<? extends IFilter> automaticDataFilterType, //
            final Class<? extends IUniversalConstants> universalConstantsType,//
            final Properties props) throws Exception {
        super(defaultHibernateTypes, applicationDomainProvider, serialisationClassProviderType, automaticDataFilterType, null, props);
        this.universalConstantsType = universalConstantsType;
        this.domainTypes = domainTypes;
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
    public ApplicationServerModule(final Map<Class, Class> defaultHibernateTypes, //
            final IApplicationDomainProvider applicationDomainProvider,//
            final List<Class<? extends AbstractEntity<?>>> domainTypes,//
            final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
            final Class<? extends IFilter> automaticDataFilterType, //
            final Properties props) throws Exception {
        this(defaultHibernateTypes, applicationDomainProvider, domainTypes, serialisationClassProviderType, automaticDataFilterType, null, props);
    }

    @Override
    protected void configure() {
        super.configure();

        /////////////////////////////// application specific ////////////////////////////
        // bind IWebApp instance with defined masters / centres and other DSL-defined configuration
        bind(IWebApp.class).toInstance(webApp);

        // bind IUserProvider
        bind(IUserProvider.class).to(ThreadLocalUserProvider.class).in(Scopes.SINGLETON);

        if (universalConstantsType != null) {
            bind(IUniversalConstants.class).to(universalConstantsType).in(Scopes.SINGLETON);
        }

        // dynamically bind DAO implementations for all companion objects
        for (final Class<? extends AbstractEntity<?>> entityType : domainTypes) {
            CompanionObjectAutobinder.bindDao(entityType, binder());
        }
    }

    @Override
    public void setInjector(final Injector injector) {
        super.setInjector(injector);

        // initialises centres / masters and other app-specific configuration.
        this.webApp.initConfiguration(injector);
    }
}
