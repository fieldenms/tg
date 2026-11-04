package ua.com.fielden.platform.mcp.web.oauth;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.basic.config.exceptions.ApplicationConfigurationException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/// The clients registered with the authorisation server, which are configured in the application properties.
///
/// Each client is described by a group of properties that share a key, which serves only to group them:
///
/// ```
/// mcp.clientRegistry.<key>.clientId=<client identifier>
/// mcp.clientRegistry.<key>.redirectUri=<URI>[, <URI>...]
/// mcp.clientRegistry.<key>.displayName=<name shown to users>
/// ```
///
/// Property `displayName` is optional, and defaults to the client identifier.
/// Property `redirectUri` lists one or more redirect URIs, separated by commas.
///
/// For example, Claude Code can be registered as follows:
///
/// ```
/// mcp.clientRegistry.claudeCode.clientId=claude_code
/// mcp.clientRegistry.claudeCode.redirectUri=http://localhost/callback
/// mcp.clientRegistry.claudeCode.displayName=Claude Code
/// ```
///
/// Any misconfiguration, such as a missing or unknown property, or a client identifier registered twice, prevents the application from starting.
///
public final class McpOAuthClientRegistry {

    public static final String MCP_CLIENT_REGISTRY = "mcp.clientRegistry";

    /// Properties that describe a client.
    /// Each member is named exactly as the property it represents.
    ///
    private enum ClientProperty {
        clientId,
        redirectUri,
        displayName;

        private static final Map<String, ClientProperty> BY_NAME = Stream.of(values()).collect(toUnmodifiableMap(ClientProperty::name, identity()));

        static boolean isRecognised(final String name) {
            return BY_NAME.containsKey(name);
        }

        /// Returns the value of this property in `clientProps`, which maps property names to values, or `null` if it is absent.
        ///
        @Nullable String valueIn(final Map<String, String> clientProps) {
            return clientProps.get(name());
        }
    }

    private final Map<String, McpOAuthClient> clientsById;

    private McpOAuthClientRegistry(final Map<String, McpOAuthClient> clientsById) {
        this.clientsById = clientsById;
    }

    /// Creates a registry from the application properties.
    ///
    public static McpOAuthClientRegistry fromProperties(final Properties props) {
        final var prefix = MCP_CLIENT_REGISTRY + ".";
        // Client key -> property name -> property value.
        final var propsByKey = props.stringPropertyNames().stream()
                .filter(name -> name.startsWith(prefix))
                .collect(groupingBy(name -> clientKey(name.substring(prefix.length())),
                                    toMap(name -> StringUtils.substringAfterLast(name, "."), props::getProperty)));

        final var clientsById = new HashMap<String, McpOAuthClient>();
        propsByKey.forEach((key, clientProps) -> {
            final var client = makeClient(key, clientProps);
            if (clientsById.putIfAbsent(client.clientId(), client) != null) {
                throw new ApplicationConfigurationException("Client [%s] is registered more than once in [%s].".formatted(client.clientId(), MCP_CLIENT_REGISTRY));
            }
        });
        return new McpOAuthClientRegistry(Map.copyOf(clientsById));
    }

    /// Returns the client with identifier `clientId`, if it is registered.
    ///
    public Optional<McpOAuthClient> find(final String clientId) {
        return Optional.ofNullable(clientsById.get(clientId));
    }

    /// Returns `<key>` of a property named `<key>.<property>`.
    ///
    private static String clientKey(final String name) {
        final var key = StringUtils.substringBeforeLast(name, ".");
        if (key.isEmpty() || key.equals(name)) {
            throw new ApplicationConfigurationException("Property [%s.%s] must be named as [%s.<key>.<property>].".formatted(MCP_CLIENT_REGISTRY, name, MCP_CLIENT_REGISTRY));
        }
        return key;
    }

    private static McpOAuthClient makeClient(final String key, final Map<String, String> clientProps) {
        final var unknownProps = clientProps.keySet().stream().filter(name -> !ClientProperty.isRecognised(name)).sorted().toList();
        if (!unknownProps.isEmpty()) {
            throw new ApplicationConfigurationException("Unknown properties of client [%s] in [%s]: %s.".formatted(key, MCP_CLIENT_REGISTRY, unknownProps));
        }

        final var clientId = StringUtils.trimToEmpty(ClientProperty.clientId.valueIn(clientProps));
        if (clientId.isEmpty()) {
            throw new ApplicationConfigurationException("Property [%s.%s.%s] must have a value.".formatted(MCP_CLIENT_REGISTRY, key, ClientProperty.clientId));
        }

        final var redirectUris = Arrays.stream(StringUtils.split(StringUtils.defaultString(ClientProperty.redirectUri.valueIn(clientProps)), ','))
                .map(String::strip)
                .filter(uri -> !uri.isEmpty())
                .map(uri -> parseRedirectUri(key, uri))
                .toList();
        if (redirectUris.isEmpty()) {
            throw new ApplicationConfigurationException("Property [%s.%s.%s] must have a value.".formatted(MCP_CLIENT_REGISTRY, key, ClientProperty.redirectUri));
        }

        final var displayName = StringUtils.defaultIfBlank(ClientProperty.displayName.valueIn(clientProps), clientId).strip();

        return new McpOAuthClient(clientId, redirectUris, displayName);
    }

    private static URI parseRedirectUri(final String key, final String uri) {
        final URI parsed;
        try {
            parsed = new URI(uri);
        } catch (final URISyntaxException ex) {
            throw new ApplicationConfigurationException("Invalid redirect URI [%s] of client [%s] in [%s].".formatted(uri, key, MCP_CLIENT_REGISTRY), ex);
        }
        if (!parsed.isAbsolute() || parsed.getHost() == null || parsed.getRawFragment() != null) {
            throw new ApplicationConfigurationException("Redirect URI [%s] of client [%s] in [%s] must be absolute, with a host and without a fragment.".formatted(uri, key, MCP_CLIENT_REGISTRY));
        }
        return parsed;
    }

}
