package ua.com.fielden.platform.mcp.web;

import org.restlet.Request;
import org.restlet.Response;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.mcp.web.oauth.McpOAuthIocModule;
import ua.com.fielden.platform.security.user.User;

/// Authenticates HTTP requests to the MCP server.
///
/// There is no default implementation.
/// An application that enables MCP must choose one, for example, by installing [McpOAuthIocModule].
///
public interface IMcpAuthenticationModel {

    /// Authenticates an HTTP request to the MCP server.
    ///
    /// If the request is unauthenticated, this method sets a status code into `response` and may modify its other attributes.
    ///
    /// @return  successful result containing the authenticated [User] if the request was authenticated, failure otherwise
    ///
    Result authenticate(Request request, Response response);

}
