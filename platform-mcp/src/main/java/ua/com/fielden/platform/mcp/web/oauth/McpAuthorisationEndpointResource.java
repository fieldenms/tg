package ua.com.fielden.platform.mcp.web.oauth;

import com.google.common.html.HtmlEscapers;
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
import org.restlet.resource.Post;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.mcp.McpAuthorisationCode;
import ua.com.fielden.platform.mcp.McpAuthorisationCodeCo;
import ua.com.fielden.platform.mcp.McpConfig;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.MiscUtilities;
import ua.com.fielden.platform.utils.ResourceLoader;
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
/// The user grants access to a client in two requests:
/// 1. `GET` -- the authorisation request from the client.
///    It is validated, and the user is shown a consent page, whose form carries the parameters of the request.
/// 2. `POST` -- the submission of the consent form, with the user's decision.
///    The submitted parameters are validated again, as nothing ties them to the original request.
///    If the user allowed access, an authorisation code is generated and saved as an instance of [McpAuthorisationCode].
///
/// In both requests, an invalid request is reported by redirecting to the client with an error, unless the client itself is invalid.
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
        resource,
        /// The user's decision in a consent form submission: `allow` or `deny`.
        decision;

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

    private static final String
            HEADER_X_FRAME_OPTIONS = "X-Frame-Options",
            HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy",
            HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

    static final String ERR_INVALID_CLIENT = "The application that sent you here is not registered, or it asked to return you to an address that is not registered for it.";

    /// Template of the consent page, with placeholders `@action`, `@client`, `@resource`, `@user` and `@hidden.fields`.
    ///
    private static final String CONSENT_PAGE_PATH = "mcp/oauth-consent.html";

    /// Prohibits framing of the consent page, to prevent clickjacking, and loading of anything but its inline styles, to contain injected content.
    ///
    private static final String CONSENT_PAGE_CONTENT_SECURITY_POLICY = "default-src 'none'; style-src 'unsafe-inline'; frame-ancestors 'none'";

    private final String consentPage;
    private final IMcpOAuth mcpOAuth;
    private final McpConfig mcpConfig;
    private final String appUri;
    private final ICompanionObjectFinder coFinder;
    private final IUserProvider userProvider;

    private McpAuthorisationEndpointResource(
            final String consentPage,
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
        this.consentPage = consentPage;
        this.mcpOAuth = mcpOAuth;
        this.mcpConfig = mcpConfig;
        this.appUri = appUri;
        this.coFinder = coFinder;
        this.userProvider = userProvider;
    }

    public static class Factory extends Restlet {

        // The consent page template is the same for all requests, so it is loaded just once.
        private final String consentPage = ResourceLoader.getText(CONSENT_PAGE_PATH);
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

            if (Method.GET.equals(request.getMethod()) || Method.POST.equals(request.getMethod()))  {
                new McpAuthorisationEndpointResource(
                        consentPage,
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

    /// Handles an authorisation request by responding with a consent page.
    ///
    @Get
    public Representation get() {
        // TODO Handle requests without an established user session.

        final var params = Param.collect(getQuery());
        final var clientResult = validateClient(params);
        if (!clientResult.isSuccessful()) {
            return invalidClient(clientResult);
        }
        final McpOAuthClient client = clientResult.getInstanceOrElseThrow();
        return validateRequest(params).orElseGet(() -> consentPage(client, params));
    }

    /// Handles a submission of the consent form by issuing an authorisation code if the user allowed access.
    ///
    /// The user's decision is acted upon right after the client is validated, so a user who denied access gets `access_denied` even for an otherwise invalid request.
    /// Any decision other than `allow` is treated as `deny`, so a code is issued only on the user's explicit permission.
    ///
    @Post
    public Representation post(final Representation input) {
        if (input == null || !MediaType.APPLICATION_WWW_FORM.equals(input.getMediaType(), true)) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("Content type must be [%s].".formatted(MediaType.APPLICATION_WWW_FORM));
        }

        final var params = Param.collect(new Form(input));
        final var clientResult = validateClient(params);
        if (!clientResult.isSuccessful()) {
            return invalidClient(clientResult);
        }
        if (!List.of("allow").equals(params.get(Param.decision))) {
            return errorRedirect(params, "access_denied", Optional.of("The user denied access."));
        }
        return validateRequest(params).orElseGet(() -> issueCode(params));
    }

    /// Validates the client and its redirect URI.
    /// This validation must precede any other, as errors can be reported to the client by redirecting to its URI only after it is validated.
    /// Otherwise, the server could be used for open redirect attacks.
    ///
    /// @return a successful result with the [McpOAuthClient], or a failure to be reported with [#invalidClient(Result)]
    ///
    private Result validateClient(final Map<Param, List<String>> params) {
        return mcpOAuth.validateClient(Param.client_id.firstValueIn(params), Param.redirect_uri.firstValueIn(params));
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

    /// Validates the authorisation request of a valid client.
    ///
    /// @return an error response, which redirects to the client, if the request is invalid
    ///
    private Optional<Representation> validateRequest(final Map<Param, List<String>> params) {
        if (userProvider.getUser() == null) {
            LOGGER.error("The current user is missing in a request to the authorisation endpoint.");
            return Optional.of(errorRedirect(params, "server_error", Optional.empty()));
        }

        final var duplicateParams = params.entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .toList();
        if (!duplicateParams.isEmpty()) {
            return Optional.of(errorRedirect(params, "invalid_request", Optional.of("Duplicate parameters: %s".formatted(CollectionUtil.toString(duplicateParams, ", ")))));
        }

        final var responseType = Param.response_type.firstValueIn(params);
        if (responseType == null) {
            return Optional.of(errorRedirect(params, "invalid_request", Optional.of("Missing parameter [%s].".formatted(Param.response_type))));
        }
        if (!"code".equals(responseType)) {
            return Optional.of(errorRedirect(params, "unsupported_response_type", Optional.of("Parameter [%s] must have value [code].".formatted(Param.response_type))));
        }

        final var codeChallengeMethod = Param.code_challenge_method.firstValueIn(params);
        if (codeChallengeMethod == null) {
            return Optional.of(errorRedirect(params, "invalid_request", Optional.of("Missing parameter [%s].".formatted(Param.code_challenge_method))));
        }
        if (!"S256".equals(codeChallengeMethod)) {
            return Optional.of(errorRedirect(params, "invalid_request", Optional.of("Parameter [%s] must have value [S256].".formatted(Param.code_challenge_method))));
        }

        final var codeChallenge = Param.code_challenge.firstValueIn(params);
        if (codeChallenge == null) {
            return Optional.of(errorRedirect(params, "invalid_request", Optional.of("Missing parameter [%s].".formatted(Param.code_challenge))));
        }
        if (!S256_CODE_CHALLENGE_PATTERN.matcher(codeChallenge).matches()) {
            return Optional.of(errorRedirect(params, "invalid_request", Optional.of("Parameter [%s] is malformed.".formatted(Param.code_challenge))));
        }

        // TODO Support scope.
        if (Param.scope.firstValueIn(params) != null) {
            return Optional.of(errorRedirect(params, "invalid_request", Optional.of("Scopes are not supported.")));
        }

        final var resource = Param.resource.firstValueIn(params);
        if (resource == null) {
            return Optional.of(errorRedirect(params, "invalid_request", Optional.of("Missing parameter [%s].".formatted(Param.resource))));
        }
        if (!resource.equals(uriPath(appUri, mcpConfig.resourcePath()))) {
            return Optional.of(errorRedirect(params, "invalid_target", Optional.of("Parameter [%s] does not represent a valid target.".formatted(Param.resource))));
        }

        // TODO Check if the current user is authorised to grant MCP access.
        //      If not, error code access_denied.

        return Optional.empty();
    }

    /// Creates the consent page for a valid authorisation request.
    /// Its form carries the parameters of the request in hidden fields, and is submitted to this endpoint.
    ///
    /// Each value is HTML-escaped, as the parameters are controlled by the client.
    /// Placeholders are replaced in a single pass, so that a value that contains a placeholder is not replaced in turn.
    ///
    private Representation consentPage(final McpOAuthClient client, final Map<Param, List<String>> params) {
        final var hiddenFields = params.keySet().stream()
                .filter(param -> param != Param.decision)
                .filter(param -> param.firstValueIn(params) != null)
                .map(param -> "<input type=\"hidden\" name=\"%s\" value=\"%s\">".formatted(param, escapeHtml(param.firstValueIn(params))))
                .collect(joining("\n"));
        final var page = StringUtils.replaceEach(
                consentPage,
                new String[] {"@action", "@client", "@resource", "@user", "@hidden.fields"},
                new String[] {
                        escapeHtml(MiscUtilities.mkUri(mcpOAuth.authorisationServerMetadata().authorisationEndpoint()).getRawPath()),
                        escapeHtml(client.displayName()),
                        escapeHtml(Param.resource.firstValueIn(params)),
                        escapeHtml(userProvider.getUser().getKey()),
                        hiddenFields});

        getResponse().getHeaders().add(HEADER_X_FRAME_OPTIONS, "DENY");
        getResponse().getHeaders().add(HEADER_CONTENT_SECURITY_POLICY, CONSENT_PAGE_CONTENT_SECURITY_POLICY);
        getResponse().setCacheDirectives(List.of(CacheDirective.noStore()));
        return new StringRepresentation(page, MediaType.TEXT_HTML, null, CharacterSet.UTF_8);
    }

    private static String escapeHtml(final String value) {
        return HtmlEscapers.htmlEscaper().escape(value);
    }

    /// Generates and saves an authorisation code for a valid authorisation request, and redirects to the client with it.
    ///
    private Representation issueCode(final Map<Param, List<String>> params) {
        final var code = saveAuthorisationCode(newAuthorisationCode(userProvider.getUser()));
        return redirect(Param.redirect_uri.firstValueIn(params),
                        Stream.of(t2("code", code.getCode()),
                                  t2("state", Param.state.firstValueIn(params)),
                                  t2("iss", mcpOAuth.authorisationServerMetadata().issuer())));
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

    /// Creates an error response for a request of a valid client.
    /// The response is formed as described in [OAuth 2.1, Section 4.1.2.1](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-13#section-4.1.2.1).
    ///
    /// ```
    /// 302 Found
    /// Location: <redirect_uri>?error=<code>&error_description=<text>&state=<state>&iss=<issuer>
    /// ```
    ///
    private Representation errorRedirect(final Map<Param, List<String>> params, final String errorCode, final Optional<String> errorDesc) {
        return redirect(Param.redirect_uri.firstValueIn(params),
                        Stream.of(t2("error", errorCode),
                                  t2("error_description", errorDesc.orElse(null)),
                                  t2("state", Param.state.firstValueIn(params)),
                                  t2("iss", mcpOAuth.authorisationServerMetadata().issuer())));
    }

    /// Redirects to `uri` with query `parameters`, omitting those without a value.
    ///
    /// A consent form submission is redirected with `303 See Other`, so that the browser follows the redirect with `GET`,
    /// as recommended by [RFC 9700, 4.12.](https://datatracker.ietf.org/doc/html/rfc9700#section-4.12).
    ///
    private Representation redirect(final String uri, final Stream<T2<String, String>> parameters) {
        getResponse().setStatus(Method.POST.equals(getRequest().getMethod()) ? Status.REDIRECTION_SEE_OTHER : Status.REDIRECTION_FOUND);
        getResponse().setLocationRef(mkUri(uri, parameters.filter(t2 -> StringUtils.isNotEmpty(t2._2())).collect(toMap())));
        return new EmptyRepresentation();
    }

    private static String mkUri(final String uri, final Map<String, String> parameters) {
        // TODO Escape parameters
        return "%s?%s".formatted(uri, parameters.entrySet().stream().map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue())).collect(joining("&")));
    }

}
