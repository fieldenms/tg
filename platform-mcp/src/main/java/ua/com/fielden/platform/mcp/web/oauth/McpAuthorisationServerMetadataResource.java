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

/// A web resource handling authorisation server metadata requests, as specified in [RFC 8414](https://datatracker.ietf.org/doc/html/rfc8414).
///
public class McpAuthorisationServerMetadataResource extends AbstractWebResource {

    private final IMcpOAuth mcpOAuth;

    protected McpAuthorisationServerMetadataResource(
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
                new McpAuthorisationServerMetadataResource(
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
        final var metadata = mcpOAuth.authorisationServerMetadata();
        return new JsonRepresentation(Map.of(
                "issuer", metadata.issuer(),
                "authorization_endpoint", metadata.authorisationEndpoint(),
                "token_endpoint", metadata.tokenEndpoint(),
                "response_types_supported", metadata.responseTypesSupported(),
                "code_challenge_methods_supported", metadata.codeChallengeMethodsSupported(),
                "grant_types_supported", metadata.grantTypesSupported(),
                "token_endpoint_auth_methods_supported", metadata.tokenEndpointAuthMethodsSupported(),
                "authorization_response_iss_parameter_supported", metadata.authorisationResponseIssParameterSupported()));
    }

}
