package ua.com.fielden.platform.web.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Header;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.routing.Filter;
import org.restlet.util.Series;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/// A filter that protects state-changing requests from Cross-Site Request Forgery (CSRF).
///
/// Web UI requests are authenticated by the `authenticator` cookie, which browsers attach to requests regardless of the site that initiates them.
/// This filter rejects requests with an unsafe method (e.g. `POST`, `PUT`, `DELETE`) with status `403 Forbidden` when the browser indicates that they originate from a different site.
/// Requests with safe methods (`GET`, `HEAD`, `OPTIONS`, `TRACE`) always pass.
///
/// The origin of a request is determined from the headers that browsers set and web pages cannot forge:
///
/// 1. `Sec-Fetch-Site`, if present, must be `same-origin` or `none` (a request initiated by the user directly, such as from a bookmark).
///    Values `same-site` and `cross-site` are rejected.
/// 2. Otherwise, `Origin`, if present, must match the host of the request.
///    The host is taken from `X-Forwarded-Host`, if present, to account for reverse proxies that rewrite `Host`, and from the request host reference otherwise.
///    Host names are compared case-insensitively; ports are compared only if the request host specifies one, as the port of a TLS-terminating reverse proxy is not visible to the application server.
///    An opaque origin (`Origin: null`) does not match.
/// 3. Requests carrying neither header are rejected.
///    Browsers capable of running the web client send `Origin` with every request that has an unsafe method (the value is `null` under some referrer policies, which does not match).
///    Non-browser clients authenticating with the session cookie need to send `Origin` matching the host of the application.
///
/// This filter is wired by [ua.com.fielden.platform.web.application.AbstractWebUiResources] in front of the resource guard, so that rejected requests do not reach authentication.
/// The login and password reset resources, which applications attach outside of this filter, apply the same check in their factories with [#rejectIfForged(Request, Response)].
/// This protects against login CSRF, where another site signs the victim in to the account of an attacker.
///
public class CsrfProtectionFilter extends Filter {
    private static final Logger LOGGER = LogManager.getLogger(CsrfProtectionFilter.class);

    public static final String
            HEADER_SEC_FETCH_SITE = "Sec-Fetch-Site",
            HEADER_ORIGIN = "Origin",
            HEADER_X_FORWARDED_HOST = "X-Forwarded-Host";

    public static final String
            SEC_FETCH_SITE_SAME_ORIGIN = "same-origin",
            SEC_FETCH_SITE_NONE = "none";

    public CsrfProtectionFilter(final Context context, final Restlet next) {
        super(context, next);
    }

    @Override
    protected int beforeHandle(final Request request, final Response response) {
        return rejectIfForged(request, response) ? STOP : CONTINUE;
    }

    /// Rejects `request` with status `403 Forbidden` if it has an unsafe method and does not originate from the same origin as the application, as described in [CsrfProtectionFilter].
    ///
    /// @return `true` if `request` was rejected, in which case it should not be handled further
    ///
    public static boolean rejectIfForged(final Request request, final Response response) {
        if (request.getMethod().isSafe() || isSameOriginRequest(request)) {
            return false;
        }

        LOGGER.warn(() -> "Rejected a potentially forged cross-site request [%s %s] from [%s] (%s: [%s], %s: [%s]).".formatted(
                request.getMethod(), request.getResourceRef(), request.getClientInfo().getAddress(),
                HEADER_SEC_FETCH_SITE, request.getHeaders().getFirstValue(HEADER_SEC_FETCH_SITE, true),
                HEADER_ORIGIN, request.getHeaders().getFirstValue(HEADER_ORIGIN, true)));
        response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        return true;
    }

    /// Determines whether `request` originates from the same origin as the application, as described in [CsrfProtectionFilter].
    ///
    static boolean isSameOriginRequest(final Request request) {
        final Series<Header> headers = request.getHeaders();

        final String fetchSite = headers.getFirstValue(HEADER_SEC_FETCH_SITE, true);
        if (fetchSite != null) {
            return SEC_FETCH_SITE_SAME_ORIGIN.equalsIgnoreCase(fetchSite.trim()) || SEC_FETCH_SITE_NONE.equalsIgnoreCase(fetchSite.trim());
        }

        final String origin = headers.getFirstValue(HEADER_ORIGIN, true);
        if (origin == null) {
            return false;
        }

        return parseUri(origin.trim())
                .flatMap(originUri -> requestHost(request).map(hostUri -> sameHost(originUri, hostUri)))
                .orElse(false);
    }

    /// Returns the host of `request` as a URI with an authority only.
    ///
    private static Optional<URI> requestHost(final Request request) {
        final String forwardedHost = request.getHeaders().getFirstValue(HEADER_X_FORWARDED_HOST, true);
        if (forwardedHost != null && !forwardedHost.isBlank()) {
            // A chain of proxies produces a comma-separated list, where the first value is the host requested by the client.
            return parseUri("//" + forwardedHost.split(",")[0].trim());
        }

        final Reference hostRef = request.getHostRef();
        if (hostRef == null || hostRef.getHostDomain() == null) {
            return Optional.empty();
        }
        return parseUri("//" + hostRef.getHostDomain() + (hostRef.getHostPort() != -1 ? ":" + hostRef.getHostPort() : ""));
    }

    private static Optional<URI> parseUri(final String value) {
        try {
            return Optional.of(new URI(value));
        } catch (final URISyntaxException ex) {
            return Optional.empty();
        }
    }

    private static boolean sameHost(final URI origin, final URI host) {
        if (host.getHost() == null || !host.getHost().equalsIgnoreCase(origin.getHost())) {
            return false;
        }
        if (host.getPort() == -1) {
            return true;
        }
        final int originPort = origin.getPort() != -1 ? origin.getPort() : ("https".equalsIgnoreCase(origin.getScheme()) ? 443 : 80);
        return originPort == host.getPort();
    }

}
