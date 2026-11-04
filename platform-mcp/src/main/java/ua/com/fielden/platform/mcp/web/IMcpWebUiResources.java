package ua.com.fielden.platform.mcp.web;

import org.restlet.Context;
import org.restlet.routing.Router;
import ua.com.fielden.platform.ioc.IocUtils;
import ua.com.fielden.platform.web.application.AbstractWebUiResources;

/// Web UI resources configuration for the Model Context Protocol (MCP) integration.
///
/// This interface is bound as a **multibinding**, as it may have multiple instances.
/// Therefore, users should request a dependency of type `Set<IMcpWebUiResources>`, or retrieve it with [IocUtils#multi].
///
/// The platform does not register these resources.
/// An application should register every instance by itself by calling [#registerUnguarded] within [AbstractWebUiResources#registerUnguardedDomainWebResources]
/// and [#registerGuarded] within [AbstractWebUiResources#registerDomainWebResources].
///
public interface IMcpWebUiResources {

    /// Registers MCP web resources that are accessible without logging in to the application.
    ///
    void registerUnguarded(Router router, Context context);

    /// Registers MCP web resources that require the user to be logged in to the application.
    ///
    void registerGuarded(Router router, Context context);

}
