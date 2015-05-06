package ua.com.fielden.platform.web.application;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

import ua.com.fielden.platform.entity.functional.centre.IQueryRunner;
import ua.com.fielden.platform.entity.functional.centre.QueryRunner;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.factories.MainWebUiComponentResourceFactory;
import ua.com.fielden.platform.web.factories.AppIndexResourceFactory;
import ua.com.fielden.platform.web.factories.WebUiPreferencesResourceFactory;
import ua.com.fielden.platform.web.factories.webui.CentreComponentResourceFactory;
import ua.com.fielden.platform.web.factories.webui.CentreResourceFactory;
import ua.com.fielden.platform.web.factories.webui.CriteriaResourceFactory;
import ua.com.fielden.platform.web.factories.webui.EgiExampleResourceFactory;
import ua.com.fielden.platform.web.factories.webui.EntityAutocompletionResourceFactory;
import ua.com.fielden.platform.web.factories.webui.EntityResourceFactory;
import ua.com.fielden.platform.web.factories.webui.EntityValidationResourceFactory;
import ua.com.fielden.platform.web.factories.webui.FileResourceFactory;
import ua.com.fielden.platform.web.factories.webui.FunctionalEntityResourceFactory;
import ua.com.fielden.platform.web.factories.webui.MasterComponentResourceFactory;
import ua.com.fielden.platform.web.factories.webui.SerialisationTestResourceFactory;
import ua.com.fielden.platform.web.factories.webui.TgReflectorComponentResourceFactory;

import com.google.inject.Injector;

/**
 * Represents the web application that is running on the server as a resource provider for browser client. Extend this abstract web application in order to provide custom entity
 * centres, entity masters and other custom views.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebApplication extends Application {

    protected final Injector injector;
    /**
     * The paths for any kind of file resources those are needed for browser client. These are mapped to the '/resources/' router path. Also these resource paths might be augmented
     * with other custom paths. When client asks for a resource then this application will search for that resource in these paths starting from the custom ones.
     */
    private final Set<String> resourcePaths = new LinkedHashSet<>();

    protected final Logger logger = Logger.getLogger(getClass());
    private final IWebUiConfig webApp;

    /**
     * Creates an instance of {@link AbstractWebApplication} with custom application name, description, author, owner and resource paths.
     *
     * @param context
     * @param injector
     * @param resourcePaths
     *            - additional root paths for file resources. (see {@link #resourcePaths} for more information).
     * @param appName
     *            - meaningful application name.
     * @param desc
     *            - short description for this application.
     * @param owner
     *            - the application owner.
     * @param author
     *            - the application author
     * @param username
     *            - TODO will be removed later after the authentication mechanism for web browser client will be implemented.
     */
    public AbstractWebApplication(
            final Context context,
            final Injector injector,
            final String[] resourcePaths,
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
        this.resourcePaths.addAll(Arrays.asList("", "ua/com/fielden/platform/web/"));
        this.resourcePaths.addAll(Arrays.asList(resourcePaths));
        setName(appName);
        setDescription(desc);
        setOwner(owner);
        setAuthor(author);
    }

    /**
     * Creates router and configures it with default resources and their paths.
     *
     */
    @Override
    public final Restlet createInboundRoot() {
        // Create router and web application for registering resources.
        final Router router = new Router(getContext());

        // Attach main application resource.
        router.attach("/", new AppIndexResourceFactory(webApp));
        router.attach("/tg-web-app/tg-app-config.html", new WebUiPreferencesResourceFactory(webApp));
        router.attach("/app/tg-app.html", new MainWebUiComponentResourceFactory(webApp));

        // Registering entity centres:
        attachCentreResources(router, webApp);

        // Registering entity masters:
        attachMasterResources(router, webApp);

        // Registering autocompletion resources:
        attachAutocompletionResources(router, webApp);

        // Registering web models.
        //attachCustomWebViewResources(router, webApp);

        // TODO Register entity masters and other custom views.

        // Attach resource for entity centre query runner.
        // TODO This is a spike resource. Must be replaced with generic functional entity resource.
        router.attach("/users/{username}/QueryRunner", new FunctionalEntityResourceFactory<QueryRunner, IQueryRunner>(IQueryRunner.class, injector));
        attachFunctionalEntities(router, injector);

        // Register resources those are in resource paths.
        attacheResources(router);

        // serialisation testing resource
        router.attach("/test/serialisation", new SerialisationTestResourceFactory(injector));
        //For egi example TODO remove later.
        router.attach("/test/egi", new EgiExampleResourceFactory(injector));
        // type meta info resource
        router.attach("/tg-reflector", new TgReflectorComponentResourceFactory(injector));

        return router;
    }

    /**
     * Attach custom functional entities to the router.
     *
     * @param router
     * @param injector
     */
    protected abstract void attachFunctionalEntities(final Router router, final Injector injector);

    /**
     * Attaches all resources relevant to entity masters (entity resource, entity validation resource, UI resources etc.).
     *
     * @param router
     * @param masters
     */
    private void attachMasterResources(final Router router, final IWebUiConfig webApp) {
        logger.info("\t\tEntity master resources attaching...");
        router.attach("/users/{username}/entity/{entityType}/{entity-id}", new EntityResourceFactory(webApp, injector));
        router.attach("/users/{username}/validation/{entityType}", new EntityValidationResourceFactory(webApp, injector));
        router.attach("/users/{username}/master/{entityType}", new MasterComponentResourceFactory(webApp));
    }

    /**
     * Attaches all resources relevant to autocompletion.
     *
     * @param router
     * @param webApp
     */
    private void attachAutocompletionResources(final Router router, final IWebUiConfig webApp) {
        logger.info("\t\tAutocompletion resources attaching...");
        router.attach("/users/{{username}}/autocompletion/{{entityType}}/{{property}}", new EntityAutocompletionResourceFactory(webApp, injector));
    }

    /**
     * Configures router for entity centre resource.
     *
     * @param router
     * @param webApp
     *            - holds the entity centre configurations.
     */
    private void attachCentreResources(final Router router, final IWebUiConfig webApp) {
        logger.info("\t\tCentre resources attaching...");
        router.attach("/users/{username}/criteria/{mitype}", new CriteriaResourceFactory(webApp, injector));
        router.attach("/users/{username}/centre/{mitype}", new CentreResourceFactory(webApp, injector));
        router.attach("/users/{username}/centre_ui/{mitype}", new CentreComponentResourceFactory(webApp, injector));
    }

    /**
     * Configures router for file resources needed for web browser client.
     *
     * @param router
     */
    private void attacheResources(final Router router) {
        logger.info("\t\tResources attaching for:..." + "\n\t\t" + StringUtils.join(resourcePaths, "/\n\t\t") + "/");
        router.attach("/resources/", new FileResourceFactory(Collections.unmodifiableSet(resourcePaths)), Template.MODE_STARTS_WITH);
    }
}