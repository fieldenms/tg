package ua.com.fielden.platform.mcp.web.oauth;

import jakarta.inject.Inject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.webui.AbstractWebResource;

import java.util.Map;

/// A web resource handling protected resource metadata requests, as specified in [RFC 9728](https://datatracker.ietf.org/doc/html/rfc9728#section-3.1).
///
public class McpProtectedResourceMetadataResource extends AbstractWebResource {

    private final IMcpOAuth mcpOAuth;

    protected McpProtectedResourceMetadataResource(
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final IMcpOAuth mcpOAuth,
            final Context context,
            final Request request,
            final Response response)
    {
        super(context, request, response, deviceProvider, dates);
        this.mcpOAuth = mcpOAuth;
    }

    public static class Factory extends Restlet {

        private final IDeviceProvider deviceProvider;
        private final IDates dates;
        private final IMcpOAuth mcpOAuth;

        @Inject
        protected Factory(final IDeviceProvider deviceProvider, final IDates dates, final IMcpOAuth mcpOAuth) {
            this.deviceProvider = deviceProvider;
            this.dates = dates;
            this.mcpOAuth = mcpOAuth;
        }

        @Override
        public void handle(final Request request, final Response response) {
            super.handle(request, response);

            if (Method.GET.equals(request.getMethod()))  {
                new McpProtectedResourceMetadataResource(
                        deviceProvider,
                        dates,
                        mcpOAuth,
                        getContext(),
                        request,
                        response
                ).handle();
            }
        }

    }

    @Get
    public Representation get() {
        final var protectedResourceMetadata = mcpOAuth.protectedResourceMetadata();
        return new JsonRepresentation(Map.of(
                "resource", protectedResourceMetadata.resource(),
                "authorization_servers", protectedResourceMetadata.authorisationServers()));
    }

}
