package ua.com.fielden.platform.mcp.web.oauth;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/// A client registered with the authorisation server.
///
/// @param clientId      the client identifier, as sent in parameter `client_id`
/// @param redirectUris  absolute URIs without a fragment, to which the client may ask to be redirected
/// @param displayName   the name of the client shown to users
///
/// @see McpOAuthClientRegistry
///
public record McpOAuthClient(String clientId, List<URI> redirectUris, String displayName) {

    /// Hosts of loopback redirect URIs, which are used by native clients, as described in [OAuth 2.1, 8.4.2.](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-13#section-8.4.2).
    ///
    private static final Set<String> LOOPBACK_HOSTS = Set.of("localhost", "127.0.0.1", "[::1]");

    public McpOAuthClient {
        redirectUris = List.copyOf(redirectUris);
    }

    /// Determines whether `uri` exactly matches a registered redirect URI.
    /// The port of a loopback URI is ignored, as native clients listen on a port that is chosen at runtime.
    ///
    public boolean isRedirectUriRegistered(final String uri) {
        final URI requested;
        try {
            requested = new URI(uri);
        } catch (final URISyntaxException ex) {
            return false;
        }
        return redirectUris.stream().anyMatch(registered -> isLoopback(registered)
                                                            ? equalsIgnoringPort(registered, requested)
                                                            : registered.toString().equals(uri));
    }

    private static boolean isLoopback(final URI uri) {
        return LOOPBACK_HOSTS.contains(uri.getHost());
    }

    private static boolean equalsIgnoringPort(final URI uri1, final URI uri2) {
        return Objects.equals(uri1.getScheme(), uri2.getScheme())
               && Objects.equals(uri1.getRawUserInfo(), uri2.getRawUserInfo())
               && Objects.equals(uri1.getHost(), uri2.getHost())
               && Objects.equals(uri1.getRawPath(), uri2.getRawPath())
               && Objects.equals(uri1.getRawQuery(), uri2.getRawQuery())
               && Objects.equals(uri1.getRawFragment(), uri2.getRawFragment());
    }

}
