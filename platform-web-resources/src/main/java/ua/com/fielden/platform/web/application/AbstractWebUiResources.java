package ua.com.fielden.platform.web.application;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.security.Authenticator;

import com.google.inject.Injector;

import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.factories.webui.AppIndexResourceFactory;
import ua.com.fielden.platform.web.factories.webui.CentreComponentResourceFactory;
import ua.com.fielden.platform.web.factories.webui.CentreResourceFactory;
import ua.com.fielden.platform.web.factories.webui.CriteriaResourceFactory;
import ua.com.fielden.platform.web.factories.webui.CustomViewResourceFactory;
import ua.com.fielden.platform.web.factories.webui.DesktopApplicationStartupResourcesComponentResourceFactory;
import ua.com.fielden.platform.web.factories.webui.EgiExampleResourceFactory;
import ua.com.fielden.platform.web.factories.webui.EntityAutocompletionResourceFactory;
import ua.com.fielden.platform.web.factories.webui.EntityResourceFactory;
import ua.com.fielden.platform.web.factories.webui.EntityValidationResourceFactory;
import ua.com.fielden.platform.web.factories.webui.FileResourceFactory;
import ua.com.fielden.platform.web.factories.webui.MainWebUiComponentResourceFactory;
import ua.com.fielden.platform.web.factories.webui.MasterComponentResourceFactory;
import ua.com.fielden.platform.web.factories.webui.MasterTestsComponentResourceFactory;
import ua.com.fielden.platform.web.factories.webui.SerialisationTestResourceFactory;
import ua.com.fielden.platform.web.factories.webui.TgReflectorComponentResourceFactory;
import ua.com.fielden.platform.web.factories.webui.WebUiPreferencesResourceFactory;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.security.DefaultWebResourceGuard;

/**
 * Represents a web application that is running on the server.
 * It is responsible for request routing and serving web resources.
 * <p>
 * This abstract implementation should be extend in concrete TG-based web applications to registers domain specific entity centres, entity masters and other views.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebUiResources extends Application {
    protected final Injector injector;

    protected final Logger logger = Logger.getLogger(getClass());
    private final IWebUiConfig webApp;
    private final ISourceController sourceController;
    protected final IUserProvider userProvider;
    protected final IDeviceProvider deviceProvider;

    /**
     * Creates an instance of {@link AbstractWebUiResources} with custom application name, description, author, owner and resource paths.
     *
     * @param context
     * @param injector
     * @param appName
     *            - meaningful application name.
     * @param desc
     *            - short description for this application.
     * @param owner
     *            - the application owner.
     * @param author
     *            - the application author
     */
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

        this.sourceController = injector.getInstance(ISourceController.class);
        this.userProvider = injector.getInstance(IUserProvider.class);
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);

        setName(appName);
        setDescription(desc);
        setOwner(owner);
        setAuthor(author);
    }

    /**
     * An insertion point for registering a domain specific web resources. The provided router is guarded, making all domain web resources automatically secure.
     *
     * @param router
     * @param webApp2
     */
    protected void registerDomainWebResources(final Router router, final IWebUiConfig webApp) {
        // The implementation is empty to ensure backward compatibility with existing projects.
    }

    /**
     * Creates the application router and configures it with default web resources.
     *
     */
    @Override
    public final Restlet createInboundRoot() {
        // Create router and web application for registering resources.
        final Router router = new Router(getContext());

        final RestServerUtil restUtil = injector.getInstance(RestServerUtil.class);

        // Attach main application resource.
        router.attach("/", new AppIndexResourceFactory(sourceController, webApp, userProvider, deviceProvider));
        router.attach("/app/tg-app-config.js", new WebUiPreferencesResourceFactory(sourceController, deviceProvider));
        router.attach("/app/tg-app.html", new MainWebUiComponentResourceFactory(sourceController, deviceProvider));
        // type meta info resource
        router.attach("/app/tg-reflector.js", new TgReflectorComponentResourceFactory(sourceController, deviceProvider));
        router.attach("/app/desktop-application-startup-resources.html", new DesktopApplicationStartupResourcesComponentResourceFactory(sourceController, deviceProvider));

        // serialisation testing resource
        router.attach("/test/serialisation", new SerialisationTestResourceFactory(injector));
        // For egi example TODO remove later.
        router.attach("/test/egi", new EgiExampleResourceFactory(injector));

        // Registering entity centres:
        attachCentreResources(router, webApp, restUtil);

        // Registering entity masters:
        attachMasterResources(router, webApp, restUtil);

        // Registering custom views:
        attachCustomViewResources(router, restUtil);

        // Registering autocompletion resources:
        attachAutocompletionResources(router, webApp);

        // register domain specific resources if any
        registerDomainWebResources(router, webApp);

        // attache internal components and related resources
        //final Set<String> webComponents = new HashSet<>();
        //webComponents.addAll(Arrays.asList("", "ua/com/fielden/platform/web/"));
        //router.attach("/resources/", new FileResourceFactory(Collections.unmodifiableSet(webComponents)), Template.MODE_STARTS_WITH);
        ///////////////////////////////////////////
        /////////// Configuring the guard /////////
        ///////////////////////////////////////////
        final Authenticator guard = new DefaultWebResourceGuard(getContext(), webApp.getDomainName(), webApp.getPath(), injector);
        guard.setNext(router);

        final Router mainRouter = new Router(getContext());
        // standard Polymer components and other resources should not be guarded
        // Register resources those are in resource paths.
        attachResources(mainRouter);

        mainRouter.attach(guard);

        return mainRouter;
    }

    /**
     * Attaches all resources relevant to entity masters (entity resource, entity validation resource, UI resources etc.).
     *
     * @param router
     * @param masters
     */
    private void attachMasterResources(final Router router, final IWebUiConfig webUiConfig, final RestServerUtil restUtil) {
        logger.info("\t\tEntity master resources attaching...");
        router.attach("/entity/{entityType}/{entity-id}", new EntityResourceFactory(webUiConfig, injector));
        router.attach("/validation/{entityType}", new EntityValidationResourceFactory(webUiConfig, injector));
        router.attach("/master_ui/Test_TgPersistentEntityWithProperties", new MasterTestsComponentResourceFactory(injector));
        router.attach("/master_ui/{entityType}", new MasterComponentResourceFactory(sourceController, restUtil, deviceProvider));
    }

    private void attachCustomViewResources(final Router router, final RestServerUtil restUtil) {
        router.attach("/custom_view/{viewName}", new CustomViewResourceFactory(sourceController, restUtil, deviceProvider));
    }

    /**
     * Attaches all resources relevant to autocompletion.
     *
     * @param router
     * @param webUiConfig
     */
    private void attachAutocompletionResources(final Router router, final IWebUiConfig webUiConfig) {
        logger.info("\t\tAutocompletion resources attaching...");
        router.attach("/autocompletion/{type}/{property}", new EntityAutocompletionResourceFactory(webUiConfig, injector));
    }

    /**
     * Configures router for entity centre resources.
     *
     * @param router
     * @param webUiConfig
     */
    private void attachCentreResources(final Router router, final IWebUiConfig webUiConfig, final RestServerUtil restUtil) {
        logger.info("\t\tCentre resources attaching...");
        router.attach("/criteria/{mitype}/{saveAsName}", new CriteriaResourceFactory(webUiConfig, injector));
        router.attach("/centre/{mitype}/{saveAsName}", new CentreResourceFactory(webUiConfig, injector));
        router.attach("/centre_ui/{mitype}", new CentreComponentResourceFactory(sourceController, restUtil, deviceProvider));
    }

    /**
     * Configures router for file resources needed for web browser client.
     *
     * @param router
     */
    private void attachResources(final Router router) {
        logger.info("\t\tResources attaching for following resource paths:" + "\n\t\t|" + StringUtils.join(webApp.resourcePaths(), "|\n\t\t|") + "|\n");
        router.attach("/resources/", new FileResourceFactory(sourceController, webApp.resourcePaths(), deviceProvider), Template.MODE_STARTS_WITH);
    }
}