package ua.com.fielden.platform.mcp.web.oauth;

import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;
import ua.com.fielden.platform.mcp.web.IMcpAuthenticationModel;
import ua.com.fielden.platform.mcp.web.McpResource;

import java.util.Properties;

import static com.google.inject.multibindings.OptionalBinder.newOptionalBinder;
import static ua.com.fielden.platform.ioc.IocUtils.PropertySource.*;
import static ua.com.fielden.platform.ioc.IocUtils.findProperty;
import static ua.com.fielden.platform.mcp.McpConfig.MCP_RESOURCE_PATH;

/// An IoC module that adds OAuth-based authentication to the MCP server.
///
/// This module can be installed even if MCP is disabled, but it will create only empty, optional bindings.
///
public class McpOAuthIocModule extends AbstractPlatformIocModule {

    private final Properties props;

    public McpOAuthIocModule(final Properties props) {
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();

        newOptionalBinder(binder(), IMcpAuthenticationModel.class);
        newOptionalBinder(binder(), McpResource.Factory.class);

        final var maybeMcpResourcePath = findProperty(MCP_RESOURCE_PATH, properties(props), system(), env());
        if (maybeMcpResourcePath.isPresent()) {
            bind(IMcpAuthenticationModel.class).to(McpOAuthAuthenticationModel.class);
        }
    }

}
