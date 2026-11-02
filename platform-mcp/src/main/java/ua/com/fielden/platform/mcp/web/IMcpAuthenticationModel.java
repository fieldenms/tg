package ua.com.fielden.platform.mcp.web;

import com.google.inject.ImplementedBy;
import org.restlet.Request;
import org.restlet.Response;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;

/// Authenticates HTTP requests to the MCP server.
///
@ImplementedBy(DefaultMcpAuthenticationModel.class)
public interface IMcpAuthenticationModel {

    /// Authenticates an HTTP request to the MCP server.
    ///
    /// If the request is unauthenticated, this method sets a status code into `response` and may modify its other attributes.
    ///
    /// @return  successful result containing the authenticated [User] if the request was authenticated, failure otherwise
    ///
    Result authenticate(Request request, Response response);

}
