package ua.com.fielden.platform.mcp.web.oauth;

import org.junit.Test;
import ua.com.fielden.platform.basic.config.exceptions.ApplicationConfigurationException;
import ua.com.fielden.platform.mcp.McpConfig;

import java.net.URI;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class McpOAuthClientRegistryTest {

    @Test
    public void client_is_registered_with_all_its_properties() {
        final var registry = McpOAuthClientRegistry.fromProperties(props(
                "mcp.clientRegistry.claudeCode.clientId", "claude_code",
                "mcp.clientRegistry.claudeCode.redirectUri", "http://localhost/callback, https://claude.ai/callback",
                "mcp.clientRegistry.claudeCode.displayName", "Claude Code",
                "mcp.resource.path", "/mcp"));

        final var client = registry.find("claude_code").orElseThrow();
        assertEquals("claude_code", client.clientId());
        assertEquals(List.of(URI.create("http://localhost/callback"), URI.create("https://claude.ai/callback")), client.redirectUris());
        assertEquals("Claude Code", client.displayName());
    }

    @Test
    public void client_is_found_by_its_identifier_rather_than_its_key() {
        final var registry = McpOAuthClientRegistry.fromProperties(props(
                "mcp.clientRegistry.claudeCode.clientId", "claude_code",
                "mcp.clientRegistry.claudeCode.redirectUri", "http://localhost/callback"));

        assertTrue(registry.find("claude_code").isPresent());
        assertTrue(registry.find("claudeCode").isEmpty());
    }

    @Test
    public void display_name_defaults_to_client_identifier() {
        final var registry = McpOAuthClientRegistry.fromProperties(props(
                "mcp.clientRegistry.claudeCode.clientId", "claude_code",
                "mcp.clientRegistry.claudeCode.redirectUri", "http://localhost/callback"));

        assertEquals("claude_code", registry.find("claude_code").orElseThrow().displayName());
    }

    @Test
    public void several_clients_can_be_registered() {
        final var registry = McpOAuthClientRegistry.fromProperties(props(
                "mcp.clientRegistry.a.clientId", "client_a",
                "mcp.clientRegistry.a.redirectUri", "http://localhost/a",
                "mcp.clientRegistry.b.clientId", "client_b",
                "mcp.clientRegistry.b.redirectUri", "http://localhost/b"));

        assertEquals(List.of(URI.create("http://localhost/a")), registry.find("client_a").orElseThrow().redirectUris());
        assertEquals(List.of(URI.create("http://localhost/b")), registry.find("client_b").orElseThrow().redirectUris());
    }

    @Test
    public void client_without_identifier_is_rejected() {
        final var ex = assertThrows(ApplicationConfigurationException.class, () -> McpOAuthClientRegistry.fromProperties(props(
                "mcp.clientRegistry.claudeCode.redirectUri", "http://localhost/callback")));
        assertEquals("Property [mcp.clientRegistry.claudeCode.clientId] must have a value.", ex.getMessage());
    }

    @Test
    public void client_without_redirect_uri_is_rejected() {
        final var ex = assertThrows(ApplicationConfigurationException.class, () -> McpOAuthClientRegistry.fromProperties(props(
                "mcp.clientRegistry.claudeCode.clientId", "claude_code",
                "mcp.clientRegistry.claudeCode.redirectUri", " , ")));
        assertEquals("Property [mcp.clientRegistry.claudeCode.redirectUri] must have a value.", ex.getMessage());
    }

    @Test
    public void unknown_client_property_is_rejected() {
        final var ex = assertThrows(ApplicationConfigurationException.class, () -> McpOAuthClientRegistry.fromProperties(props(
                "mcp.clientRegistry.claudeCode.clientId", "claude_code",
                "mcp.clientRegistry.claudeCode.redirectUrl", "http://localhost/callback")));
        assertEquals("Unknown properties of client [claudeCode] in [mcp.clientRegistry]: [redirectUrl].", ex.getMessage());
    }

    @Test
    public void property_without_client_key_is_rejected() {
        final var ex = assertThrows(ApplicationConfigurationException.class, () -> McpOAuthClientRegistry.fromProperties(props(
                "mcp.clientRegistry.clientId", "claude_code")));
        assertEquals("Property [mcp.clientRegistry.clientId] must be named as [mcp.clientRegistry.<key>.<property>].", ex.getMessage());
    }

    @Test
    public void client_identifier_registered_twice_is_rejected() {
        final var ex = assertThrows(ApplicationConfigurationException.class, () -> McpOAuthClientRegistry.fromProperties(props(
                "mcp.clientRegistry.a.clientId", "claude_code",
                "mcp.clientRegistry.a.redirectUri", "http://localhost/a",
                "mcp.clientRegistry.b.clientId", "claude_code",
                "mcp.clientRegistry.b.redirectUri", "http://localhost/b")));
        assertEquals("Client [claude_code] is registered more than once in [mcp.clientRegistry].", ex.getMessage());
    }

    @Test
    public void redirect_uri_must_be_absolute_with_a_host_and_without_a_fragment() {
        for (final var uri : List.of("/callback", "//localhost/callback", "urn:callback", "http://localhost/callback#x")) {
            final var ex = assertThrows(uri, ApplicationConfigurationException.class, () -> McpOAuthClientRegistry.fromProperties(props(
                    "mcp.clientRegistry.claudeCode.clientId", "claude_code",
                    "mcp.clientRegistry.claudeCode.redirectUri", uri)));
            assertEquals("Redirect URI [%s] of client [claudeCode] in [mcp.clientRegistry] must be absolute, with a host and without a fragment.".formatted(uri), ex.getMessage());
        }
    }

    @Test
    public void malformed_redirect_uri_is_rejected() {
        final var ex = assertThrows(ApplicationConfigurationException.class, () -> McpOAuthClientRegistry.fromProperties(props(
                "mcp.clientRegistry.claudeCode.clientId", "claude_code",
                "mcp.clientRegistry.claudeCode.redirectUri", "http://local host/callback")));
        assertEquals("Invalid redirect URI [http://local host/callback] of client [claudeCode] in [mcp.clientRegistry].", ex.getMessage());
    }

    @Test
    public void redirect_uri_of_non_loopback_host_must_match_exactly() {
        final var client = clientWithRedirectUris("https://app.com:8443/callback?x=1");

        assertTrue(client.isRedirectUriRegistered("https://app.com:8443/callback?x=1"));
        assertFalse(client.isRedirectUriRegistered("https://app.com:9443/callback?x=1"));
        assertFalse(client.isRedirectUriRegistered("https://app.com/callback?x=1"));
        assertFalse(client.isRedirectUriRegistered("https://app.com:8443/callback?x=2"));
        assertFalse(client.isRedirectUriRegistered("https://app.com:8443/callback/?x=1"));
        assertFalse(client.isRedirectUriRegistered("HTTPS://app.com:8443/callback?x=1"));
    }

    @Test
    public void redirect_uri_of_loopback_host_may_have_any_port() {
        for (final var host : List.of("localhost", "127.0.0.1", "[::1]")) {
            final var client = clientWithRedirectUris("http://%s/callback".formatted(host));

            assertTrue(host, client.isRedirectUriRegistered("http://%s/callback".formatted(host)));
            assertTrue(host, client.isRedirectUriRegistered("http://%s:33418/callback".formatted(host)));
        }
    }

    @Test
    public void redirect_uri_of_loopback_host_must_otherwise_match_exactly() {
        final var client = clientWithRedirectUris("http://localhost/callback");

        assertFalse(client.isRedirectUriRegistered("http://127.0.0.1:33418/callback"));
        assertFalse(client.isRedirectUriRegistered("https://localhost:33418/callback"));
        assertFalse(client.isRedirectUriRegistered("http://localhost:33418/other"));
        assertFalse(client.isRedirectUriRegistered("http://localhost:33418/callback?x=1"));
        assertFalse(client.isRedirectUriRegistered("http://localhost:33418/callback#x"));
        assertFalse(client.isRedirectUriRegistered("http://user@localhost:33418/callback"));
    }

    @Test
    public void redirect_uri_may_match_any_of_registered_ones() {
        final var client = clientWithRedirectUris("https://app.com/a", "https://app.com/b");

        assertTrue(client.isRedirectUriRegistered("https://app.com/a"));
        assertTrue(client.isRedirectUriRegistered("https://app.com/b"));
    }

    @Test
    public void malformed_redirect_uri_is_not_registered() {
        assertFalse(clientWithRedirectUris("http://localhost/callback").isRedirectUriRegistered("http://local host/callback"));
    }

    @Test
    public void validateClient_returns_registered_client() {
        final var mcpOAuth = mcpOAuth();
        final McpOAuthClient client = mcpOAuth.validateClient("claude_code").getInstanceOrElseThrow();
        assertEquals("claude_code", client.clientId());
        assertSame(client, mcpOAuth.validateClient("claude_code", "http://localhost:33418/callback").getInstanceOrElseThrow());
    }

    @Test
    public void validateClient_fails_for_missing_or_unknown_client() {
        final var mcpOAuth = mcpOAuth();
        assertEquals("Missing client identifier.", mcpOAuth.validateClient(null).getMessage());
        assertEquals("Unknown client [other].", mcpOAuth.validateClient("other").getMessage());
        assertEquals("Unknown client [other].", mcpOAuth.validateClient("other", "http://localhost/callback").getMessage());
    }

    @Test
    public void validateClient_fails_for_missing_or_unregistered_redirect_uri() {
        final var mcpOAuth = mcpOAuth();
        assertEquals("Missing redirect URI.", mcpOAuth.validateClient("claude_code", null).getMessage());
        assertEquals("Redirect URI [http://localhost/other] is not registered for client [claude_code].",
                     mcpOAuth.validateClient("claude_code", "http://localhost/other").getMessage());
    }

    private static Properties props(final String... keysAndValues) {
        final var props = new Properties();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            props.setProperty(keysAndValues[i], keysAndValues[i + 1]);
        }
        return props;
    }

    private static McpOAuthClient clientWithRedirectUris(final String... uris) {
        return new McpOAuthClient("client", List.of(uris).stream().map(URI::create).toList(), "Client");
    }

    private static DefaultMcpOAuth mcpOAuth() {
        return new DefaultMcpOAuth(
                "https://app.com",
                new McpConfig("/mcp"),
                McpOAuthClientRegistry.fromProperties(props(
                        "mcp.clientRegistry.claudeCode.clientId", "claude_code",
                        "mcp.clientRegistry.claudeCode.redirectUri", "http://localhost/callback")));
    }

}
