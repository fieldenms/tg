package ua.com.fielden.platform.mcp.web.oauth;

import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.*;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.mcp.*;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.annotations.AppUri;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.webui.AbstractWebResource;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.utils.MiscUtilities.uriPath;

/// Web resource for the [OAuth 2.1. Token Endpoint](https://www.ietf.org/archive/id/draft-ietf-oauth-v2-1-13.html#name-token-endpoint).
///
/// A POST request to the token endpoint is processed as follows:
/// 1. Validation of request parameters.
/// 2. Access token issuance: an instance of [McpAccessToken] is created and saved, and the token is sent to the client.
///
/// Every response carries `Cache-Control: no-store`, as required for responses containing tokens or credentials.
///
public class McpTokenEndpointResource extends AbstractWebResource {

    /// Recognised request parameters.
    /// Each member is named exactly as the parameter it represents.
    ///
    private enum Param {
        grant_type,
        client_id,
        code,
        code_verifier,
        redirect_uri,
        resource;

        private static final Map<String, Param> BY_NAME = Stream.of(values()).collect(toUnmodifiableMap(Param::name, identity()));

        /// Groups the values of parameters in `form` by parameter.
        /// Unrecognised parameters are ignored, and parameters without a value are treated as omitted.
        ///
        static Map<Param, List<String>> collect(final Form form) {
            return form.stream()
                    .filter(param -> BY_NAME.containsKey(param.getName()) && StringUtils.isNotEmpty(param.getValue()))
                    .collect(groupingBy(param -> BY_NAME.get(param.getName()),
                                        () -> new EnumMap<>(Param.class),
                                        mapping(Parameter::getValue, toList())));
        }

        /// Returns the first value of this parameter in `params`, or `null` if it is absent.
        ///
        @Nullable String firstValueIn(final Map<Param, List<String>> params) {
            final var values = params.get(this);
            return values == null ? null : values.getFirst();
        }
    }

    /// A code verifier, as defined by [RFC 7636, 4.1.](https://datatracker.ietf.org/doc/html/rfc7636#section-4.1).
    ///
    private static final Pattern CODE_VERIFIER = Pattern.compile("[A-Za-z0-9\\-._~]{43,128}");

    private static final int ACCESS_TOKEN_BYTE_LENGTH = 32;
    private static final int ACCESS_TOKEN_LIFETIME_HOURS = 72;

    private final IMcpOAuth mcpOAuth;
    private final McpConfig mcpConfig;
    private final String appUri;
    private final ICompanionObjectFinder coFinder;
    private final IUserProvider userProvider;

    private McpTokenEndpointResource(
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final IMcpOAuth mcpOAuth,
            final McpConfig mcpConfig,
            final String appUri,
            final ICompanionObjectFinder coFinder,
            final IUserProvider userProvider,
            final Context context,
            final Request request,
            final Response response)
    {
        super(context, request, response, deviceProvider, dates);
        this.mcpOAuth = mcpOAuth;
        this.mcpConfig = mcpConfig;
        this.appUri = appUri;
        this.coFinder = coFinder;
        this.userProvider = userProvider;
    }

    public static class Factory extends Restlet {

        private final IDeviceProvider deviceProvider;
        private final IDates dates;
        private final IMcpOAuth mcpOAuth;
        private final McpConfig mcpConfig;
        private final String appUri;
        private final ICompanionObjectFinder coFinder;
        private final IUserProvider userProvider;

        @Inject
        protected Factory(
                final IDeviceProvider deviceProvider,
                final IDates dates,
                final IMcpOAuth mcpOAuth,
                final McpConfig mcpConfig,
                final @AppUri String appUri,
                final ICompanionObjectFinder coFinder,
                final IUserProvider userProvider)
        {
            this.deviceProvider = deviceProvider;
            this.dates = dates;
            this.mcpOAuth = mcpOAuth;
            this.mcpConfig = mcpConfig;
            this.appUri = appUri;
            this.coFinder = coFinder;
            this.userProvider = userProvider;
        }

        @Override
        public void handle(final Request request, final Response response) {
            super.handle(request, response);

            if (Method.POST.equals(request.getMethod()))  {
                new McpTokenEndpointResource(
                        deviceProvider,
                        dates,
                        mcpOAuth,
                        mcpConfig,
                        appUri,
                        coFinder,
                        userProvider,
                        getContext(),
                        request,
                        response
                ).handle();
            }
        }

    }

    @Post
    public Representation post(final Representation input) {
        getResponse().setCacheDirectives(List.of(CacheDirective.noStore()));

        if (input == null || !MediaType.APPLICATION_WWW_FORM.equals(input.getMediaType(), true)) {
            return error("invalid_request", "Content type must be [%s].".formatted(MediaType.APPLICATION_WWW_FORM));
        }

        final var params = Param.collect(new Form(input));
        final var duplicateParams = params.entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .toList();
        if (!duplicateParams.isEmpty()) {
            return error("invalid_request", "Duplicate parameters: %s".formatted(CollectionUtil.toString(duplicateParams, ", ")));
        }

        final var grantType = Param.grant_type.firstValueIn(params);
        final var clientId = Param.client_id.firstValueIn(params);
        final var code = Param.code.firstValueIn(params);
        final var codeVerifier = Param.code_verifier.firstValueIn(params);
        final var redirectUri = Param.redirect_uri.firstValueIn(params);
        final var resource = Param.resource.firstValueIn(params);

        if (grantType == null) {
            return error("invalid_request", "Missing parameter [%s].".formatted(Param.grant_type));
        }
        if (!"authorization_code".equals(grantType)) {
            return error("unsupported_grant_type", "Parameter [%s] must have value [authorization_code].".formatted(Param.grant_type));
        }

        if (clientId == null) {
            return error("invalid_request", "Missing parameter [%s].".formatted(Param.client_id));
        }
        final var validateClientResult = mcpOAuth.validateClient(clientId);
        if (!validateClientResult.isSuccessful()) {
            return error("invalid_client", "Invalid client. %s".formatted(validateClientResult.getMessage()));
        }

        if (code == null) {
            return error("invalid_request", "Missing parameter [%s].".formatted(Param.code));
        }
        // TODO Redeem the code atomically before the remaining checks.
        //      If it was already redeemed, error code invalid_grant and revoke the access token issued from it.
        final McpAuthorisationCodeCo coCode = coFinder.find(McpAuthorisationCode.class, true);
        final var authorisationCode = coCode.findByKeyAndFetch(fetch(McpAuthorisationCode.class).with(McpAuthorisationCode.USER, fetchKeyAndDescOnly(User.class)), code);
        if (authorisationCode == null || !dates.serverNow().toDate().before(authorisationCode.getExpirationDate())) {
            return error("invalid_grant", "Authorisation code is invalid or expired.");
        }
        // TODO Check that the code was issued to client_id.
        //      If not, error code invalid_grant.

        if (codeVerifier == null) {
            return error("invalid_request", "Missing parameter [%s].".formatted(Param.code_verifier));
        }
        if (!CODE_VERIFIER.matcher(codeVerifier).matches()) {
            return error("invalid_request", "Parameter [%s] is malformed.".formatted(Param.code_verifier));
        }
        // TODO Compare BASE64URL(SHA-256(code_verifier)) with the code challenge stored with the code, in constant time.
        //      If they differ, error code invalid_grant.

        if (redirectUri != null) {
            // TODO Compare with the redirect URI stored with the code.
            final var r = mcpOAuth.validateClient(clientId, redirectUri);
            if (!r.isSuccessful()) {
                return error("invalid_grant", "Invalid redirect URI. %s".formatted(r.getMessage()));
            }
        }

        if (resource == null) {
            return error("invalid_request", "Missing parameter [%s].".formatted(Param.resource));
        }
        // TODO Compare with the resource stored with the code.
        if (!resource.equals(uriPath(appUri, mcpConfig.resourcePath()))) {
            return error("invalid_target", "Parameter [%s] does not represent a valid target.".formatted(Param.resource));
        }

        final var accessToken = new BearerAccessToken(ACCESS_TOKEN_BYTE_LENGTH, ACCESS_TOKEN_LIFETIME_HOURS * 3600L, null);
        // The access token is saved on behalf of the user who granted the authorisation.
        final var prevUser = userProvider.getUser();
        try {
            userProvider.setUsername(authorisationCode.getUser().getKey(), coFinder.find(User.class, true));
            saveAccessToken(accessToken, authorisationCode);
        } finally {
            userProvider.setUser(prevUser);
        }
        return new JsonRepresentation(accessToken.toJSONObject());
    }

    /// Saves `accessToken` issued in exchange for `authorisationCode`.
    /// Only the hash of the token value is saved.
    ///
    private McpAccessToken saveAccessToken(final BearerAccessToken accessToken, final McpAuthorisationCode authorisationCode) {
        final McpAccessTokenCo co = coFinder.find(McpAccessToken.class, false);
        return co.save(co.new_()
                         .setTokenHash(co.tokenHash(accessToken.getValue()))
                         .setExpirationDate(dates.serverNow().plusHours(ACCESS_TOKEN_LIFETIME_HOURS).toDate())
                         .setAuthorisationCode(authorisationCode));
    }

    /// Creates an error response, as described in [OAuth 2.1, Section 3.2.4](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-13#section-3.2.4).
    ///
    /// ```
    /// 400 Bad Request
    /// Content-Type: application/json
    ///
    /// {"error": <code>, "error_description": <text>}
    /// ```
    ///
    private Representation error(final String errorCode, final String errorDesc) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return new JsonRepresentation(Map.<String, Object>of(
                "error", errorCode,
                "error_description", errorDesc));
    }

}
