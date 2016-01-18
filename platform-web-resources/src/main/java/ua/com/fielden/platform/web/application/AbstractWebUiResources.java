package ua.com.fielden.platform.web.application;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.security.Authenticator;

import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.factories.FileProcessingResourceFactory;
import ua.com.fielden.platform.web.factories.webui.AppIndexResourceFactory;
import ua.com.fielden.platform.web.factories.webui.CentreComponentResourceFactory;
import ua.com.fielden.platform.web.factories.webui.CentreEgiResourceFactory;
import ua.com.fielden.platform.web.factories.webui.CentreResourceFactory;
import ua.com.fielden.platform.web.factories.webui.CriteriaResourceFactory;
import ua.com.fielden.platform.web.factories.webui.EgiExampleResourceFactory;
import ua.com.fielden.platform.web.factories.webui.EntityAutocompletionResourceFactory;
import ua.com.fielden.platform.web.factories.webui.EntityResourceFactory;
import ua.com.fielden.platform.web.factories.webui.EntityValidationResourceFactory;
import ua.com.fielden.platform.web.factories.webui.FileResourceFactory;
import ua.com.fielden.platform.web.factories.webui.MainWebUiComponentResourceFactory;
import ua.com.fielden.platform.web.factories.webui.MasterComponentResourceFactory;
import ua.com.fielden.platform.web.factories.webui.MasterTestsComponentResourceFactory;
import ua.com.fielden.platform.web.factories.webui.SerialisationTestResourceFactory;
import ua.com.fielden.platform.web.factories.webui.TgElementLoaderComponentResourceFactory;
import ua.com.fielden.platform.web.factories.webui.TgReflectorComponentResourceFactory;
import ua.com.fielden.platform.web.factories.webui.WebUiPreferencesResourceFactory;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.security.DefaultWebResourceGuard;

import com.google.inject.Injector;

/**
 * Represents the web application that is running on the server as a resource provider for browser client. Extend this abstract web application in order to provide custom entity
 * centres, entity masters and other custom views.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebUiResources extends Application {
    protected final Injector injector;

    protected final Logger logger = Logger.getLogger(getClass());
    private final IWebUiConfig webApp;
    private final ISourceController sourceController;

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

        setName(appName);
        setDescription(desc);
        setOwner(owner);
        setAuthor(author);
    }
    
    
    /**
     * An insertion point for registering a domain specific web resources.
     * The provided router is guarded, making all domain web resources automatically secure.
     * 
     * @param router
     */
    protected void registerDomainWebResources(final Router router) {
        // The implementation is empty to ensure backward compatibility with existing projects.
    }

    /**
     * Creates router and configures it with default resources and their paths.
     *
     */
    @Override
    public final Restlet createInboundRoot() {
        // Create router and web application for registering resources.
        final Router router = new Router(getContext());

        final RestServerUtil restUtil = injector.getInstance(RestServerUtil.class);

        // Attach main application resource.
        router.attach("/", new AppIndexResourceFactory(sourceController, restUtil));
        router.attach("/app/tg-app-config.html", new WebUiPreferencesResourceFactory(sourceController, restUtil));
        router.attach("/app/tg-app.html", new MainWebUiComponentResourceFactory(sourceController, restUtil));
        // type meta info resource
        router.attach("/app/tg-reflector.html", new TgReflectorComponentResourceFactory(sourceController, restUtil));
        router.attach("/app/tg-element-loader.html", new TgElementLoaderComponentResourceFactory(sourceController, restUtil));

        // serialisation testing resource
        router.attach("/test/serialisation", new SerialisationTestResourceFactory(injector));
        // For egi example TODO remove later.
        router.attach("/test/egi", new EgiExampleResourceFactory(injector));

        // Registering entity centres:
        attachCentreResources(router, webApp, restUtil);

        // Registering entity masters:
        attachMasterResources(router, webApp, restUtil);

        // Registering autocompletion resources:
        attachAutocompletionResources(router, webApp);

        // register domain specific resources if any
        registerDomainWebResources(router);
        
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
        attachResources(mainRouter, restUtil);

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
        router.attach("/master_ui/{entityType}", new MasterComponentResourceFactory(sourceController, restUtil));
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
        router.attach("/criteria/{mitype}", new CriteriaResourceFactory(webUiConfig, injector));
        router.attach("/centre/{mitype}", new CentreResourceFactory(webUiConfig, injector));
        router.attach("/centre_ui/{mitype}", new CentreComponentResourceFactory(sourceController, restUtil));
        router.attach("/centre_ui/egi/{mitype}", new CentreEgiResourceFactory(sourceController, restUtil));
    }

    /**
     * Configures router for file resources needed for web browser client.
     *
     * @param router
     */
    private void attachResources(final Router router, final RestServerUtil restUtil) {
        logger.info("\t\tResources attaching for following resource paths:" + "\n\t\t|" + StringUtils.join(webApp.resourcePaths(), "|\n\t\t|") + "|\n");
        router.attach("/resources/", new FileResourceFactory(sourceController, restUtil, webApp.resourcePaths()), Template.MODE_STARTS_WITH);
    }
}