package ua.com.fielden.platform.web.application;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.security.Authenticator;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.factories.webui.*;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.security.DefaultWebResourceGuard;

/// Represents a web application that is running on the server.
/// It is responsible for request routing and serving web resources.
///
/// This abstract implementation should be extended in concrete TG-based web applications to registers domain specific entity centres, entity masters and other views.
///
public abstract class AbstractWebUiResources extends Application {
    protected final Injector injector;

    protected final Logger logger = LogManager.getLogger(getClass());
    private final IWebUiConfig webApp;
    private final IWebResourceLoader webResourceLoader;
    protected final IUserProvider userProvider;
    protected final IDeviceProvider deviceProvider;
    protected final IDates dates;

    /// Creates an instance of [AbstractWebUiResources] with custom application name, description, author, owner and resource paths.
    ///
    /// @param appName meaningful application name.
    /// @param desc short description for this application.
    /// @param owner the application owner.
    /// @param author the application author
    ///
    public AbstractWebUiResources(
            final Context context,
            final Injector injector,
            final String appName,
            final String desc,
            final String owner,
            final String author,
            final IWebUiConfig webApp) {
        super(context);
        this.webApp = webApp;
        //        this.platformJsScriptsLocation = "../../tg/platform-web-ui/src/main/web/ua/com/fielden/platform/web/";
        //        this.platformVendorJsScriptsLocation = "../../tg/platform-web-ui/src/main/resources/";
        // --> TODO not so elegant and flexible. There should be more elegant version for development and deployment. Use application.props file.
        //        this.platformGisJsScriptsLocation = platformJsScriptsLocation + "gis/";
        // --> TODO not so elegant and flexible. There should be more elegant version for development and deployment. Use application.props file.
        this.injector = injector;

        this.webResourceLoader = injector.getInstance(IWebResourceLoader.class);
        this.userProvider = injector.getInstance(IUserProvider.class);
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
        this.dates = injector.getInstance(IDates.class);

        setName(appName);
        setDescription(desc);
        setOwner(owner);
        setAuthor(author);
    }

    /// An insertion point for registering a domain specific web resources. The provided router is guarded, making all domain web resources automatically secure.
    ///
    protected void registerDomainWebResources(final Router router, final IWebUiConfig webApp) {
        // The implementation is empty to ensure backward compatibility with existing projects.
    }

    /// Creates the application router and configures it with default web resources.
    ///
    @Override
    public final Restlet createInboundRoot() {
        // Create router and web application for registering resources.
        final Router guardedRouter = new Router(getContext());

        final RestServerUtil restUtil = injector.getInstance(RestServerUtil.class);

        // Attach application configuration resource
        guardedRouter.attach("/app/configuration", new ApplicationConfigurationResourceFactory(webApp, injector));
        // Attach main application resource.
        guardedRouter.attach("/", new AppIndexResourceFactory(webResourceLoader, webApp, userProvider, deviceProvider, dates, injector.getInstance(ICriteriaGenerator.class)));
        guardedRouter.attach("/app/tg-app-config.js", new WebUiPreferencesResourceFactory(webResourceLoader, deviceProvider, dates));
        guardedRouter.attach("/app/tg-app.js", new MainWebUiComponentResourceFactory(webResourceLoader, deviceProvider, dates));
        guardedRouter.attach("/app/tg-app-actions.js", new TgAppActionsResourceFactory(webResourceLoader, deviceProvider, dates));
        // type meta info resource
        guardedRouter.attach("/app/tg-reflector.js", new TgReflectorComponentResourceFactory(webResourceLoader, deviceProvider, dates));
        guardedRouter.attach("/app/application-startup-resources.js", new ApplicationStartupResourcesComponentResourceFactory(webResourceLoader, deviceProvider, dates));

        //Attache client side error logger resource
        guardedRouter.attach("/error", new WebClientErrorLoggerResourceFactory(injector));

        // Registering entity centres:
        attachCentreResources(guardedRouter, webApp, restUtil);

        // Registering entity masters:
        attachMasterResources(guardedRouter, webApp, restUtil);

        // Registering custom views:
        attachCustomViewResources(guardedRouter, restUtil);

        // Registering autocompletion resources:
        attachAutocompletionResources(guardedRouter, webApp);

        guardedRouter.attach("/tiny/{%s}".formatted(TinyHyperlinkResourceFactory.HASH), new TinyHyperlinkResourceFactory(webApp, injector));

        if (injector.getInstance(Key.get(boolean.class, Names.named("web.api")))) { // in case where Web API has been turned-on in application.properties ...
            // ... register GraphiQL resources
            guardedRouter.attach("/graphiql", new GraphiQLResourceFactory(injector));
        }

        // register domain specific resources if any
        registerDomainWebResources(guardedRouter, webApp);

        // attache internal components and related resources
        //final Set<String> webComponents = new HashSet<>();
        //webComponents.addAll(Arrays.asList("", "ua/com/fielden/platform/web/"));
        //router.attach("/resources/", new FileResourceFactory(Collections.unmodifiableSet(webComponents)), Template.MODE_STARTS_WITH);
        ///////////////////////////////////////////
        /////////// Configuring the guard /////////
        ///////////////////////////////////////////
        final Authenticator guard = new DefaultWebResourceGuard(getContext(), webApp.getDomainName(), webApp.getPath(), injector);
        guard.setNext(guardedRouter);

        final Router mainRouter = new Router(getContext());
        // standard Polymer components and other resources should not be guarded
        // Register resources those are in resource paths.
        attachResources(mainRouter);
        mainRouter.attach("/service-worker.js", new ServiceWorkerResourceFactory(webResourceLoader, deviceProvider, dates));

        mainRouter.attach(guard);

        return mainRouter;
    }

    /// Attaches all resources relevant to entity masters (entity resource, entity validation resource, UI resources etc.).
    ///
    private void attachMasterResources(final Router router, final IWebUiConfig webUiConfig, final RestServerUtil restUtil) {
        logger.info("\t\tEntity master resources attaching...");
        router.attach("/entity/{entityType}/{entity-id}", new EntityResourceFactory(webUiConfig, injector));
        router.attach("/validation/{entityType}", new EntityValidationResourceFactory(webUiConfig, injector));
        router.attach("/master_ui/Test_TgPersistentEntityWithProperties", new MasterTestsComponentResourceFactory(injector));
        router.attach("/master_ui/{entityType}", new MasterComponentResourceFactory(webResourceLoader, restUtil, deviceProvider, dates));
    }

    private void attachCustomViewResources(final Router router, final RestServerUtil restUtil) {
        router.attach("/custom_view/{viewName}", new CustomViewResourceFactory(webResourceLoader, restUtil, deviceProvider, dates));
    }

    /// Attaches all resources relevant to autocompletion.
    ///
    private void attachAutocompletionResources(final Router router, final IWebUiConfig webUiConfig) {
        logger.info("\t\tAutocompletion resources attaching...");
        router.attach("/autocompletion/{type}/{property}", new EntityAutocompletionResourceFactory(webUiConfig, injector));
    }

    /// Configures router for entity centre resources.
    ///
    private void attachCentreResources(final Router router, final IWebUiConfig webUiConfig, final RestServerUtil restUtil) {
        logger.info("\t\tCentre resources attaching...");
        router.attach("/criteria/{mitype}/{saveAsName}", new CriteriaResourceFactory(webUiConfig, injector));
        router.attach("/centre/{mitype}/{saveAsName}", new CentreResourceFactory(webUiConfig, injector));
        router.attach("/centre_ui/{mitype}", new CentreComponentResourceFactory(webResourceLoader, restUtil, deviceProvider, dates));
    }

    /// Configures router for file resources needed for web browser client.
    ///
    private void attachResources(final Router router) {
        logger.info("\t\tResources attaching for following resource paths:" + "\n\t\t|" + StringUtils.join(webApp.resourcePaths(), "|\n\t\t|") + "|\n");
        router.attach("/resources/", new FileResourceFactory(webResourceLoader, webApp.resourcePaths(), deviceProvider, dates), Template.MODE_STARTS_WITH);
    }
}
