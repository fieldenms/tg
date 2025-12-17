package ua.com.fielden.platform.web.ioc;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.binder.AnnotatedBindingBuilder;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.menu.IMenuRetriever;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.app.SerialisationTypeEncoder;
import ua.com.fielden.platform.web.app.ThreadLocalDeviceProvider;
import ua.com.fielden.platform.web.centre.api.actions.multi.SingleActionSelector;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.interfaces.IEntityMasterUrlProvider;
import ua.com.fielden.platform.web.resources.webui.AbstractWebUiConfig;
import ua.com.fielden.platform.web.test.server.TgTestWebApplicationServerIocModule;
import ua.com.fielden.platform.web.uri.EntityMasterUrlProvider;
import ua.com.fielden.platform.web.utils.CriteriaEntityRestorer;
import ua.com.fielden.platform.web.utils.EntityCentreAPI;
import ua.com.fielden.platform.web.utils.EntityCentreAPIImpl;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

import static ua.com.fielden.platform.basic.config.Workflows.deployment;
import static ua.com.fielden.platform.basic.config.Workflows.vulcanizing;
import static ua.com.fielden.platform.reflection.CompanionObjectAutobinder.bindCo;
import static ua.com.fielden.platform.web.centre.api.actions.multi.SingleActionSelector.INSTANCE;

/**
 * This interface defines <code>Web UI</code> specific IoC binding contract,
 * which is to be implemented by an application specific IoC module, used by an application server.
 * <p>
 *  Each concrete application is expected to have a principle IoC module <code>ApplicationServerModules</code> that binds everything except the <code>Web UI</code> related dependencies.
 *  The reason the principle IoC module cannot bind these dependencies is rooted in the fact that they're not visible at the <code>DAO</code> project module, where it must reside and be used for unit tests, data population and migration utilities and more.
 *  <p>
 *  Module {@link TgTestWebApplicationServerIocModule}, which governs <code>Web UI</code> dependencies for a platform demo and test application server, can be used as an example.
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
    default void bindWebAppResources(final IWebUiConfig webApp) {
        bindType(IDeviceProvider.class).to(ThreadLocalDeviceProvider.class);

        /////////////////////////////// application specific ////////////////////////////
        // bind IWebApp instance with defined masters / centres and other DSL-defined configuration
        bindType(IWebUiConfig.class).toInstance(webApp);
        bindType(IMenuRetriever.class).toInstance(webApp);

        // bind Entity Master URI creator
        bindType(IEntityMasterUrlProvider.class).to(EntityMasterUrlProvider.class);

        bindType(IWebResourceLoader.class).to(WebResourceLoader.class);

        // dependent on IWebUiConfig, IUserProvider and other Web UI infrastructure
        bindType(ISerialisationTypeEncoder.class).to(SerialisationTypeEncoder.class);

        // dependent on IWebUiConfig, IUserProvider and other Web UI infrastructure
        bindType(ICriteriaEntityRestorer.class).to(CriteriaEntityRestorer.class);
        bindType(EntityCentreAPI.class).to(EntityCentreAPIImpl.class);

        // bind companion object implementations that are dependent on ICriteriaEntityRestorer
        PlatformDomainTypes.typesDependentOnWebUI.stream().forEach(type -> bindCo(type, (co, t) -> bindType(co).to(t)));

        // bind SingleActionSelector to its singleton
        bindType(SingleActionSelector.class).toInstance(INSTANCE);
    }

    /**
     * Initialises an already bound {@link IWebUiConfig} instance.
     * The default implementation assumes that it is of type {@link AbstractWebUiConfig}.
     *
     * @param injector
     */
    default void initWebAppWithoutCaching(final Injector injector) {
        final AbstractWebUiConfig webApp = (AbstractWebUiConfig) injector.getInstance(IWebUiConfig.class);
        webApp.setInjector(injector);
        // initialise IWebApp with its masters / centres
        webApp.initConfiguration();
    }

    /**
     * Initialises an already bound {@link IWebUiConfig} instance.
     * The default implementation assumes that it is of type {@link AbstractWebUiConfig}.
     * <p>
     * This implementation creates default configurations for all registered centres to perform early
     * caching of DomainTreeEnhancers (to avoid heavy computations later).
     * 
     * @param injector
     */
    default void initWebApp(final Injector injector) {
        initWebAppWithoutCaching(injector);
        final IWebUiConfig webUiConfig = injector.getInstance(IWebUiConfig.class);

        if (deployment == webUiConfig.workflow() || vulcanizing == webUiConfig.workflow()) {
            // let's preload heavy Entity Centre configurations in deployment mode and during vulcanisation to trigger caching of DomainTreeEnhancers to avoid heavy computations later
            webUiConfig.createDefaultConfigurationsForAllCentres();
        }
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
