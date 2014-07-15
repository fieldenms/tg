package ua.com.fielden.platform.web.application;

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
    private final String platformGisJsScriptsLocation;
    private final Logger logger = Logger.getLogger(getClass());

    public AbstractWebBrowserBasedServerApplication(final Context context, final Injector injector, final String name, final String desc, final String owner, final String author, final String username) {
        super(context);
        this.platformJsScriptsLocation = "../../tg/platform-ui/src/main/java/ua/com/fielden/platform/web/";
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
        attachAdditionalResources(router);
        attachCentreResources(eccc, serialiser, router);
        attachGisComponentResources(router);
        return router;
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
        router.attach("/map.html", new FileResourceFactory(platformGisJsScriptsLocation + "map.html", MediaType.TEXT_HTML));
        router.attach("/map.js", new FileResourceFactory(platformGisJsScriptsLocation + "map.js", MediaType.TEXT_JAVASCRIPT));
        router.attach("/map.css", new FileResourceFactory(platformGisJsScriptsLocation + "map.css", MediaType.TEXT_CSS));

        attachLeafletResources(router);
        attachLeafletMapProviderResources(router);
        attachLeafletDrawResources(router);
        attachLeafletMarkerClusterResources(router);
        attachLeafletControlLoadingResources(router);
        attachLeafletEasyButtonResources(router);
        attachLeafletMarkerRotationResources(router);

        attachInitialisationResources(router);
    }

    private void attachLeafletControlLoadingResources(final Router router) {
        logger.info("\t\t\tLeaflet.control.loading resources attaching...");
        router.attach("/leaflet/controlloading/Control.Loading.js", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/controlloading/Control.Loading.js", MediaType.TEXT_JAVASCRIPT));
        router.attach("/leaflet/controlloading/Control.Loading.css", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/controlloading/Control.Loading.css", MediaType.TEXT_CSS));
    }

    private void attachLeafletEasyButtonResources(final Router router) {
        logger.info("\t\t\tLeaflet.easy.button resources attaching...");
        router.attach("/leaflet/easybutton/easy-button.js", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/easybutton/easy-button.js", MediaType.TEXT_JAVASCRIPT));
        router.attach("/leaflet/easybutton/font-awesome.css", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/easybutton/font-awesome.css", MediaType.TEXT_CSS));
    }

    private void attachLeafletMarkerRotationResources(final Router router) {
        logger.info("\t\t\tLeaflet.marker.rotation resources attaching...");
        router.attach("/leaflet/markerrotation/Marker.Rotate.js", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/markerrotation/Marker.Rotate.js", MediaType.TEXT_JAVASCRIPT));
    }

    private void attachLeafletMarkerClusterResources(final Router router) {
        logger.info("\t\t\tLeaflet.markercluster resources attaching...");
        router.attach("/leaflet/markercluster/leaflet.markercluster.js", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/markercluster/leaflet.markercluster.js", MediaType.TEXT_JAVASCRIPT));
        router.attach("/leaflet/markercluster/leaflet.markercluster-src.js", new FileResourceFactory(platformGisJsScriptsLocation
                + "leaflet/markercluster/leaflet.markercluster-src.js", MediaType.TEXT_JAVASCRIPT));
        router.attach("/leaflet/markercluster/MarkerCluster.css", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/markercluster/MarkerCluster.css", MediaType.TEXT_CSS));
        router.attach("/leaflet/markercluster/MarkerCluster.Default.css", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/markercluster/MarkerCluster.Default.css", MediaType.TEXT_CSS));
    }

    private void attachLeafletResources(final Router router) {
        logger.info("\t\t\tLeaflet resources attaching...");
        router.attach("/leaflet/leaflet.js", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/leaflet.js", MediaType.TEXT_JAVASCRIPT));
        router.attach("/leaflet/leaflet.css", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/leaflet.css", MediaType.TEXT_CSS));
        router.attach("/leaflet/leaflet-src.js", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/leaflet-src.js", MediaType.TEXT_JAVASCRIPT));

        // TODO images
    }

    private void attachLeafletDrawResources(final Router router) {
        logger.info("\t\t\tLeaflet.draw resources attaching...");
        router.attach("/leaflet/draw/leaflet.draw.js", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/draw/leaflet.draw.js", MediaType.TEXT_JAVASCRIPT));
        router.attach("/leaflet/draw/leaflet.draw.css", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/draw/leaflet.draw.css", MediaType.TEXT_CSS));
        router.attach("/leaflet/draw/leaflet.draw-src.js", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/draw/leaflet.draw-src.js", MediaType.TEXT_JAVASCRIPT));

        // TODO images
    }

    private void attachLeafletMapProviderResources(final Router router) {
        logger.info("\t\t\tLeaflet map provider resources attaching...");
        router.attach("/leaflet/providers/Google.js", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/providers/Google.js", MediaType.TEXT_JAVASCRIPT));
        router.attach("/leaflet/providers/Yandex.js", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/providers/Yandex.js", MediaType.TEXT_JAVASCRIPT));
        router.attach("/leaflet/providers/Bing.js", new FileResourceFactory(platformGisJsScriptsLocation + "leaflet/providers/Bing.js", MediaType.TEXT_JAVASCRIPT));
    }

    private void attachInitialisationResources(final Router router) {
        logger.info("\t\t\tLeaflet initialisation json resources attaching...");
        router.attach("/init/empty.json", new FileResourceFactory(platformGisJsScriptsLocation + "init/empty.json", MediaType.TEXT_JAVASCRIPT));
        router.attach("/init/gps-track.json", new FileResourceFactory(platformGisJsScriptsLocation + "init/gps-track.json", MediaType.TEXT_JAVASCRIPT));
        router.attach("/init/geo-zones.json", new FileResourceFactory(platformGisJsScriptsLocation + "init/geo-zones.json", MediaType.TEXT_JAVASCRIPT));
        router.attach("/init/stops.json", new FileResourceFactory(platformGisJsScriptsLocation + "init/stops.json", MediaType.TEXT_JAVASCRIPT));
    }

    protected abstract void attachAdditionalResources(final Router router);

    protected Logger logger() {
        return logger;
    }
}