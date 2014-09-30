package ua.com.fielden.platform.web.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.routing.Router;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.web.CentreResourceFactory;
import ua.com.fielden.platform.web.FileResourceFactory;
import ua.com.fielden.platform.web.QueryPageResourceFactory;

import com.google.inject.Injector;

public abstract class AbstractWebBrowserBasedServerApplication extends Application {
    private final Injector injector;
    private final String username;
    private final String platformJsScriptsLocation;
    private final String platformVendorJsScriptsLocation;
    private final String platformGisJsScriptsLocation;
    private final Logger logger = Logger.getLogger(getClass());

    public AbstractWebBrowserBasedServerApplication(final Context context, final Injector injector, final String name, final String desc, final String owner, final String author, final String username) {
        super(context);
        this.platformJsScriptsLocation = "../../tg/platform-web-ui/src/main/web/ua/com/fielden/platform/web/";
        this.platformVendorJsScriptsLocation = "../../tg/platform-web-ui/src/main/resources/";
        // --> TODO not so elegant and flexible. There should be more elegant version for development and deployment. Use application.props file.
        this.platformGisJsScriptsLocation = platformJsScriptsLocation + "gis/";
        // --> TODO not so elegant and flexible. There should be more elegant version for development and deployment. Use application.props file.
        this.username = username;
        this.injector = injector;
        setName(name);
        setDescription(desc);
        setOwner(owner);
        setAuthor(author);
    }

    @Override
    public final Restlet createInboundRoot() {
        final IEntityCentreConfigController eccc = injector.getInstance(IEntityCentreConfigController.class);
        final ISerialiser serialiser = injector.getInstance(ISerialiser.class);
        final Router router = new Router(getContext());

        router.attach("/main", new FileResourceFactory(platformJsScriptsLocation + "centre/main.html", MediaType.TEXT_HTML));

        attachVendorResources(router);
        
        attachCentreResources(eccc, serialiser, router);
        attachGisComponentResources(router);
        attachAdditionalResources(router);
        return router;
    }
    
    protected void attachVendorResources(final Router router) {
        logger.info("\t\tVendor resources attaching...");
        register(router, platformVendorJsScriptsLocation, "/");
    }

    protected void attachCentreResources(final IEntityCentreConfigController eccc, final ISerialiser serialiser, final Router router) {
        logger.info("\t\tCentre resources attaching...");
        router.attach("/centre.js", new FileResourceFactory(platformJsScriptsLocation + "centre/centre.js", MediaType.TEXT_JAVASCRIPT));
        router.attach("/centre.css", new FileResourceFactory(platformJsScriptsLocation + "centre/centre.css", MediaType.TEXT_CSS));
        router.attach("/centre", new FileResourceFactory(platformJsScriptsLocation + "centre/centre.html", MediaType.TEXT_HTML));
        router.attach("/centre/{centreName}", new CentreResourceFactory(eccc, serialiser, username));
        router.attach("/centre/{centreName}/query/{page}", new QueryPageResourceFactory(injector, username));
    }

    protected void attachGisComponentResources(final Router router) {
        logger.info("\t\tGIS component resources attaching...");
        
        // register global configuration point for requirejs dependencies
        router.attach("/config.js", new FileResourceFactory(platformJsScriptsLocation + "config.js", MediaType.TEXT_JAVASCRIPT));
        
        register(router, platformGisJsScriptsLocation, "/gis/", //
                platformGisJsScriptsLocation + "spike/"); // directory to exclude
    }

    protected void register(final Router router, final String where, final String prefix, final String... excludeDirs) {
        List<String> relativeFilenames;
        try {
            relativeFilenames = determineFilenames(where, excludeDirs);
            for (final String relativeFilename : relativeFilenames) {
                logger.info("Attaching [" + where + relativeFilename + "] into [" + prefix + relativeFilename + "] with media type [" + mediaType(relativeFilename) + "]...");
                router.attach(prefix + relativeFilename, new FileResourceFactory(where + relativeFilename, mediaType(relativeFilename)));
            }
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private List<String> determineFilenames(final String where, final String... excludeDirs) throws IOException {
        final List<String> filenames = new ArrayList<String>();
        Files.walk(Paths.get(where)).forEach(filePath -> {

            if (Files.isRegularFile(filePath) && !shouldExclude(filePath, Arrays.asList(excludeDirs))) {
                final String relativeName = filePath.toString().replaceFirst(where, "");
                filenames.add(relativeName);
            }
        });
        return filenames;
    }

    private boolean shouldExclude(final Path filePath, final List<String> excludeDirs) {
        for (final String excludeDir : excludeDirs) {
            if (filePath.startsWith(excludeDir)) {
                return true;
            }
        }
        return false;
    }

    private MediaType mediaType(final String relativeFilename) {
        return relativeFilename.toLowerCase().endsWith(".png") ? MediaType.IMAGE_PNG : //
                relativeFilename.toLowerCase().endsWith(".js") ? MediaType.TEXT_JAVASCRIPT : //;
                        relativeFilename.toLowerCase().endsWith(".json") ? MediaType.TEXT_JAVASCRIPT : //;
                                relativeFilename.toLowerCase().endsWith(".html") ? MediaType.TEXT_HTML : //;
                                        relativeFilename.toLowerCase().endsWith(".css") ? MediaType.TEXT_CSS : MediaType.ALL;
    }

    protected abstract void attachAdditionalResources(final Router router);

    protected Logger logger() {
        return logger;
    }
}