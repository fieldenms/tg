package ua.com.fielden.platform.mcp.web.oauth;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.*;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.mcp.McpAuthorisationCode;
import ua.com.fielden.platform.mcp.McpAuthorisationCodeCo;
import ua.com.fielden.platform.mcp.McpConfig;
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
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T2.toMap;
import static ua.com.fielden.platform.utils.MiscUtilities.uriPath;

/// Web resource for the [OAuth 2.1. Authorization Endpoint](https://www.ietf.org/archive/id/draft-ietf-oauth-v2-1-13.html#name-authorization-endpoint).
///
/// A GET request to the authorisation endpoint is processed as follows:
/// 1. Validation: request parameters, current user's rights.
/// 2. Authorisation code generation: an instance of [McpAuthorisationCode] is created and saved.
///
public class McpAuthorisationEndpointResource extends AbstractWebResource {

    private static final Logger LOGGER = LogManager.getLogger();

    /// Recognised request parameters.
    /// Each member is named exactly as the parameter it represents.
    ///
    private enum Param {
        response_type,
        client_id,
        redirect_uri,
        code_challenge,
        code_challenge_method,
        state,
        scope,
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

    /// A code challenge for the `S256` method, as defined by [RFC 7636, 4.2.](https://datatracker.ietf.org/doc/html/rfc7636#section-4.2):
    /// the unpadded base64url encoding of a SHA-256 hash, which is always 43 characters long.
    ///
    private static final Pattern S256_CODE_CHALLENGE_PATTERN = Pattern.compile("[A-Za-z0-9\\-_]{43}");

    private static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

    static final String ERR_INVALID_CLIENT = "The application that sent you here is not registered, or it asked to return you to an address that is not registered for it.";

    private final IMcpOAuth mcpOAuth;
    private final McpConfig mcpConfig;
    private final String appUri;
    private final ICompanionObjectFinder coFinder;
    private final IUserProvider userProvider;

    private McpAuthorisationEndpointResource(
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

            if (Method.GET.equals(request.getMethod()))  {
                new McpAuthorisationEndpointResource(
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

    @Get
    public Representation get() {
        // TODO Handle requests without an established user session.
        // TODO User consent page, which should send a POST with an Allow/Deny decision.

        final var params = Param.collect(getQuery());
        final var responseType = Param.response_type.firstValueIn(params);
        final var clientId = Param.client_id.firstValueIn(params);
        final var redirectUri = Param.redirect_uri.firstValueIn(params);
        final var codeChallenge = Param.code_challenge.firstValueIn(params);
        final var codeChallengeMethod = Param.code_challenge_method.firstValueIn(params);
        final var state = Param.state.firstValueIn(params);
        final var scope = Param.scope.firstValueIn(params);
        final var resource = Param.resource.firstValueIn(params);

        final var validateClientResult = mcpOAuth.validateClient(clientId, redirectUri);
        if (!validateClientResult.isSuccessful()) {
            return invalidClient(validateClientResult);
        }

        // Error response that redirects to the client-specified URI must be used only after the client is validated,
        // to prevent open redirect attacks.
        final var mkError = errorMaker(redirectUri, Optional.ofNullable(state), Optional.of(mcpOAuth.authorisationServerMetadata().issuer()));

        final var user = userProvider.getUser();
        if (user == null) {
            LOGGER.error("The current user is missing in a request to the authorisation endpoint.");
            return mkError.apply("server_error", Optional.empty());
        }

        final var duplicateParams = params.entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .toList();
        if (!duplicateParams.isEmpty()) {
            return mkError.apply("invalid_request", Optional.of("Duplicate parameters: %s".formatted(CollectionUtil.toString(duplicateParams, ", "))));
        }

        if (responseType == null) {
            return mkError.apply("invalid_request", Optional.of("Missing parameter [%s].".formatted(Param.response_type)));
        }
        if (!"code".equals(responseType)) {
            return mkError.apply("unsupported_response_type", Optional.of("Parameter [%s] must have value [code].".formatted(Param.response_type)));
        }

        if (codeChallengeMethod == null) {
            return mkError.apply("invalid_request", Optional.of("Missing parameter [%s].".formatted(Param.code_challenge_method)));
        }
        if (!"S256".equals(codeChallengeMethod)) {
            return mkError.apply("invalid_request", Optional.of("Parameter [%s] must have value [S256].".formatted(Param.code_challenge_method)));
        }

        if (codeChallenge == null) {
            return mkError.apply("invalid_request", Optional.of("Missing parameter [%s].".formatted(Param.code_challenge)));
        }
        if (!S256_CODE_CHALLENGE_PATTERN.matcher(codeChallenge).matches()) {
            return mkError.apply("invalid_request", Optional.of("Parameter [%s] is malformed.".formatted(Param.code_challenge)));
        }

        // TODO Support scope.
        if (scope != null) {
            return mkError.apply("invalid_request", Optional.of("Scopes are not supported."));
        }

        if (resource == null) {
            return mkError.apply("invalid_request", Optional.of("Missing parameter [%s].".formatted(Param.resource)));
        }
        if (!resource.equals(uriPath(appUri, mcpConfig.resourcePath()))) {
            return mkError.apply("invalid_target", Optional.of("Parameter [%s] does not represent a valid target.".formatted(Param.resource)));
        }

        // TODO Check if the current user is authorised to grant MCP access.
        //      If not, error code access_denied.

        final var code = saveAuthorisationCode(newAuthorisationCode(user));
        getResponse().setStatus(Status.REDIRECTION_FOUND);
        getResponse().setLocationRef(mkUri(redirectUri,
                                           Stream.of(t2("code", code.getCode()),
                                                     t2("state", state),
                                                     t2("iss", mcpOAuth.authorisationServerMetadata().issuer()))
                                                  .filter(t2 -> StringUtils.isNotEmpty(t2._2()))
                                                  .collect(toMap())));
        return new EmptyRepresentation();
    }

    private static String mkUri(final String uri, final Map<String, String> parameters) {
        // TODO Escape parameters
        return "%s?%s".formatted(uri, parameters.entrySet().stream().map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue())).collect(joining("&")));
    }

    private McpAuthorisationCode newAuthorisationCode(final User user) {
        final McpAuthorisationCodeCo co = coFinder.find(McpAuthorisationCode.class, false);
        return co.new_()
                .setCode(new AuthorizationCode(32).getValue())
                .setExpirationDate(dates.serverNow().plusMinutes(10).toDate())
                .setUser(user);
    }

    private McpAuthorisationCode saveAuthorisationCode(final McpAuthorisationCode code) {
        final McpAuthorisationCodeCo co = coFinder.find(McpAuthorisationCode.class, false);
        return co.save(code);
    }

    /// Creates an error response for a failed client validation.
    /// The response is shown to the user, as the client cannot be trusted with a redirect.
    ///
    /// The response carries a fixed message, so that a crafted request cannot place its own text on a page served by the application.
    /// The details of the failure, which include values from the request, are logged with line breaks escaped, so that they cannot forge log entries.
    ///
    private Representation invalidClient(final Result failure) {
        final var details = StringUtils.replaceEach(failure.getMessage(), new String[] {"\r", "\n"}, new String[] {"\\r", "\\n"});
        LOGGER.warn("Invalid client in a request to the authorisation endpoint. {}", details);
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        getResponse().getHeaders().add(HEADER_X_CONTENT_TYPE_OPTIONS, "nosniff");
        getResponse().setCacheDirectives(List.of(CacheDirective.noStore()));
        return new StringRepresentation(ERR_INVALID_CLIENT, MediaType.TEXT_PLAIN, null, CharacterSet.UTF_8);
    }

    /// Creates a function for producing errors.
    /// The HTTP response is formed as described in [OAuth 2.1, Section 4.1.2.1](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-13#section-4.1.2.1).
    ///
    /// ```
    /// 302 Found
    /// Location: <redirect_uri>?error=<code>&error_description=<text>&state=<state>&iss=<issuer>
    /// ```
    ///
    private ErrorFn errorMaker(
            final String redirectUri,
            final Optional<String> state,
            final Optional<String> iss)
    {
        return (errorCode, errorDesc) -> {
            getResponse().setStatus(Status.REDIRECTION_FOUND);
            getResponse().setLocationRef(mkUri(redirectUri,
                                               Stream.of(t2("error", errorCode.toString()),
                                                         t2("error_description", errorDesc.orElse(null)),
                                                         t2("state", state.orElse(null)),
                                                         t2("iss", iss.orElse(null)))
                                                       .filter(t2 -> StringUtils.isNotEmpty(t2._2()))
                                                       .collect(toMap())));
            return new EmptyRepresentation();
        };
    }

    @FunctionalInterface
    private interface ErrorFn {
        Representation apply(Object errorCode, Optional<String> errorDesc);
    }

}
