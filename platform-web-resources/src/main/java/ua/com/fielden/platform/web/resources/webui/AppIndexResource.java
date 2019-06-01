package ua.com.fielden.platform.web.resources.webui;

import static org.restlet.data.MediaType.TEXT_HTML;
import static ua.com.fielden.platform.web.resources.webui.FileResource.createRepresentation;

import java.lang.management.ManagementFactory;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

/**
 * Responds to GET request with a generated application specific index resource (for desktop and mobile web apps).
 * <p>
 * The returned HTML should be thought of as <code>index.html</code> in its classical meaning.
 *
 * @author TG Team
 *
 */
public class AppIndexResource extends AbstractWebResource {
    private final IWebUiConfig webUiConfig;
    private final IUserProvider userProvider;
    private final ISourceController sourceController;
    
    /**
     * Creates {@link AppIndexResource} instance.
     *
     * @param context
     * @param request
     * @param response
     */
    public AppIndexResource(
            final ISourceController sourceController,
            final IWebUiConfig webUiConfig,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final Context context, 
            final Request request, 
            final Response response) {
        super(context, request, response, deviceProvider);
        this.webUiConfig = webUiConfig;
        this.userProvider = userProvider;
        this.sourceController = sourceController;
    }

    @Get
    @Override
    public Representation get() {
        final User currentUser = userProvider.getUser();
        if (!Workflows.deployment.equals(webUiConfig.workflow()) && !Workflows.vulcanizing.equals(webUiConfig.workflow()) && isDebugMode() && currentUser != null) {
            // if application user hits refresh -- all configurations will be cleared. This is useful when using with JRebel / Eclipse Debug -- no need to restart server after 
            //  changing Web UI configurations (all configurations should exist in scope of IWebUiConfig.initConfiguration() method).
            webUiConfig.clearConfiguration();
            webUiConfig.initConfiguration();
        }
        return createRepresentation(sourceController, TEXT_HTML, "/app/tg-app-index.html", getReference().getRemainingPart());
    }

    /**
     * Indicates whether JRebel was enabled in this instance of server JVM by using OS-specific '-agentpath:' VM argument (this is the preferred way to enable JRebel for java applications).
     * 
     * @return
     */
    private boolean isDebugMode() {
        return ManagementFactory.getRuntimeMXBean()
                .getInputArguments().toString().indexOf("jdwp") >= 0;
    }
}
