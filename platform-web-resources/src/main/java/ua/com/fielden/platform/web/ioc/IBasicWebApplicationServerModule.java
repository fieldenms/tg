package ua.com.fielden.platform.web.ioc;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.binder.AnnotatedBindingBuilder;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.ServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.EntityExportActionDao;
import ua.com.fielden.platform.entity.IEntityExportAction;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.menu.IMenuRetriever;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.app.SerialisationTypeEncoder;
import ua.com.fielden.platform.web.app.ThreadLocalDeviceProvider;
import ua.com.fielden.platform.web.centre.CentreColumnWidthConfigUpdaterDao;
import ua.com.fielden.platform.web.centre.CentreConfigEditActionDao;
import ua.com.fielden.platform.web.centre.CentreConfigLoadActionDao;
import ua.com.fielden.platform.web.centre.CentreConfigUpdaterDao;
import ua.com.fielden.platform.web.centre.ICentreColumnWidthConfigUpdater;
import ua.com.fielden.platform.web.centre.ICentreConfigEditAction;
import ua.com.fielden.platform.web.centre.ICentreConfigLoadAction;
import ua.com.fielden.platform.web.centre.ICentreConfigUpdater;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.webui.AbstractWebUiConfig;
import ua.com.fielden.platform.web.test.server.TgTestWebApplicationServerModule;
import ua.com.fielden.platform.web.test.server.WebGlobalDomainTreeManager;
import ua.com.fielden.platform.web.utils.CriteriaEntityRestorer;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/**
 * This interface defines <code>Web UI</code> specific IoC binding contract,
 * which is to be implement by an application specific IoC module, used by an application server.
 * <p>
 *  Each concrete application is expected to have a principle IoC module <code>ApplicationServerModules</code> that binds everything except the <code>Web UI</code> related dependencies.
 *  The reason the principle IoC module cannot bind these dependencies is rooted in the fact that the're not visible at the <code>DAO</code> project module, where it must reside and be used for unit tests, data population and migration utilities and more.
 *  <p>
 *  Module {@link TgTestWebApplicationServerModule}, which governs <code>Web UI</code> dependencies for a platform demo and test application server, can be used as an example.
 *
 * @author TG Team
 *
 */
public interface IBasicWebApplicationServerModule {

    /**
     * Binds all needed resources to enable {@link IWebUiConfig} logic.
     *
     * @param webApp
     */
    default public void bindWebAppResources(final IWebUiConfig webApp) {
        // bind IDeviceProvider to its implementation as singleton
        bindType(IDeviceProvider.class).to(ThreadLocalDeviceProvider.class).in(Scopes.SINGLETON);
        
        /////////////////////////////// application specific ////////////////////////////
        bindType(IServerGlobalDomainTreeManager.class).to(ServerGlobalDomainTreeManager.class).in(Scopes.SINGLETON);
        bindType(IGlobalDomainTreeManager.class).to(WebGlobalDomainTreeManager.class);

        // bind IWebApp instance with defined masters / centres and other DSL-defined configuration
        bindType(IWebUiConfig.class).toInstance(webApp);
        bindType(IMenuRetriever.class).toInstance(webApp);

        // bind ISourceController to its implementation as singleton
        bindType(ISourceController.class).to(SourceControllerImpl.class).in(Scopes.SINGLETON);

        // bind ISerialisationTypeEncoder to its implementation as singleton -- it is dependent on IServerGlobalDomainTreeManager and IUserProvider
        bindType(ISerialisationTypeEncoder.class).to(SerialisationTypeEncoder.class).in(Scopes.SINGLETON);
        
        // bind ICriteriaEntityRestorer to its implementation as singleton -- it is dependent on IWebUiConfig, IServerGlobalDomainTreeManager, IUserProvider and other Web UI infrastructure
        bindType(ICriteriaEntityRestorer.class).to(CriteriaEntityRestorer.class).in(Scopes.SINGLETON);
        // bind companion object implementations that are dependent on ICriteriaEntityRestorer
        bindType(IEntityExportAction.class).to(EntityExportActionDao.class);
        bindType(ICentreConfigUpdater.class).to(CentreConfigUpdaterDao.class);
        bindType(ICentreColumnWidthConfigUpdater.class).to(CentreColumnWidthConfigUpdaterDao.class);
        
        bindType(ICentreConfigLoadAction.class).to(CentreConfigLoadActionDao.class);
        bindType(ICentreConfigEditAction.class).to(CentreConfigEditActionDao.class);
        // TODO bindType(ICentreConfigSaveAction.class).to(CentreConfigSaveActionDao.class);
    }

    /**
     * Initialises an already bound {@link IWebUiConfig} instance.
     * The default implementation assumes that is has a concrete type {@link AbstractWebUiConfig}.
     *
     * @param injector
     */
    default public void initWebApp(final Injector injector) {
        final AbstractWebUiConfig webApp = (AbstractWebUiConfig) injector.getInstance(IWebUiConfig.class);
        webApp.setInjector(injector);

        final ISerialisationTypeEncoder serialisationTypeEncoder = injector.getInstance(ISerialisationTypeEncoder.class);
        final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache = injector.getInstance(IIdOnlyProxiedEntityTypeCache.class);
        final ISerialiser serialiser = injector.getInstance(ISerialiser.class);
        serialiser.initJacksonEngine(serialisationTypeEncoder, idOnlyProxiedEntityTypeCache);

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
