package ua.com.fielden.platform.web.resources.webui;

import java.io.ByteArrayInputStream;
import java.lang.management.ManagementFactory;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.google.common.base.Charsets;

import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
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
    private final IServerGlobalDomainTreeManager serverGdtm;
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
            final IServerGlobalDomainTreeManager serverGdtm,
            final IWebUiConfig webUiConfig,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final Context context, 
            final Request request, 
            final Response response) {
        super(context, request, response, deviceProvider);
        this.serverGdtm = serverGdtm;
        this.webUiConfig = webUiConfig;
        this.userProvider = userProvider;
        this.sourceController = sourceController;
    }

    @Override
    protected Representation get() throws ResourceException {
        final User currentUser = userProvider.getUser();
        if (!Workflows.deployment.equals(webUiConfig.workflow()) && !Workflows.vulcanizing.equals(webUiConfig.workflow()) && isDebugMode() && currentUser != null) {
            // if application user hits refresh -- all configurations will be cleared (including cahced instances of centres). This is useful when using with JRebel -- no need to restart server after 
            //  changing Web UI configurations (all configurations should exist in scope of IWebUiConfig.initConfiguration() method).
            webUiConfig.clearConfiguration(serverGdtm.get(currentUser.getId()), device());
            webUiConfig.initConfiguration();
        }
        
        final String source = sourceController.loadSource("/app/tg-app-index.html", device());
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8))));
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
