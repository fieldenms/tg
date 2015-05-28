package ua.com.fielden.platform.web.factories.webui;

import org.apache.log4j.Logger;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CentreComponentResource;

import com.google.inject.Injector;

/**
 * The server resource factory for entity centres.
 *
 * @author TG Team
 *
 */
public class CentreComponentResourceFactory extends Restlet {
    private final IWebUiConfig webApp;
    private final RestServerUtil restUtil;
    private final Logger logger = Logger.getLogger(getClass());

    /**
     * Creates the {@link CentreComponentResourceFactory} instance with map of available entity centres.
     *
     * @param centres
     */
    public CentreComponentResourceFactory(final IWebUiConfig webApp, final Injector injector) {
        this.webApp = webApp;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    /**
     * Invokes on GET request from client.
     */
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            final String mitypeString = (String) request.getAttributes().get("mitype");
            logger.debug(String.format("[%s] centre component retrieving...", mitypeString));

            final Class<? extends MiWithConfigurationSupport<?>> miType = (Class<? extends MiWithConfigurationSupport<?>>) ClassesRetriever.findClass(mitypeString);
            final EntityCentre centre = this.webApp.getCentres().get(miType);

            new CentreComponentResource(restUtil, centre, getContext(), request, response).handle();
            logger.debug(String.format("[%s] centre component retrieving...done", mitypeString));
        }
    }
}
