package ua.com.fielden.platform.mcp.ioc;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.OptionalBinder;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;
import ua.com.fielden.platform.ioc.exceptions.MissingParameterDependencyException;
import ua.com.fielden.platform.mcp.McpConfig;
import ua.com.fielden.platform.mcp.TgMcpServer;
import ua.com.fielden.platform.mcp.web.IMcpWebUiResources;
import ua.com.fielden.platform.mcp.web.McpResource;
import ua.com.fielden.platform.mcp.web.McpWebResourceGuard;
import ua.com.fielden.platform.mcp.web.McpWebUiResources;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.mcp.McpConfig.*;
import static ua.com.fielden.platform.utils.ImmutableListUtils.prepend;

/// An IoC module that provides the Model Context Protocol (MCP) server.
///
/// This module starts the MCP server on application startup.
///
/// The MCP integration can be enabled by specifying [McpConfig#MCP_RESOURCE_PATH].
///
/// The following bindings are installed:
///
/// * [McpConfig], [IMcpWebUiResources], [McpResource.Factory], [McpWebResourceGuard.Factory] -- all optional bindings,
///    present if MCP is enabled.
///
public class McpIocModule extends AbstractPlatformIocModule {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Properties properties;

    public McpIocModule(final Properties properties) {
        this.properties = properties;
    }

    @Override
    protected void configure() {
        OptionalBinder.newOptionalBinder(binder(), McpConfig.class);
        OptionalBinder.newOptionalBinder(binder(), IMcpWebUiResources.class);
        OptionalBinder.newOptionalBinder(binder(), McpResource.Factory.class);
        OptionalBinder.newOptionalBinder(binder(), McpWebResourceGuard.Factory.class);

        final var maybeMcpResourcePath = findProperty(MCP_RESOURCE_PATH, PropertySource.APPLICATION, PropertySource.SYSTEM, PropertySource.ENV);
        if (maybeMcpResourcePath.isPresent()) {
            LOGGER.info(() -> "Model Context Protocol (MCP) is enabled.");
            final var mcpResourcePath = maybeMcpResourcePath.get();
            final var mcpConfig = new McpConfig(
                    mcpResourcePath,
                    getProperty(WEB_API_KEY_MCP, PropertySource.APPLICATION, PropertySource.SYSTEM, PropertySource.ENV),
                    getProperty(MCP_USER, PropertySource.APPLICATION, PropertySource.SYSTEM, PropertySource.ENV));
            bind(McpConfig.class).toInstance(mcpConfig);
            bind(IMcpWebUiResources.class).to(McpWebUiResources.class);
            install(new FactoryModuleBuilder().build(McpResource.Factory.class));
            install(new FactoryModuleBuilder().build(McpWebResourceGuard.Factory.class));

            requestStaticInjection(McpIocModule.class);
        }
        else {
            LOGGER.info(() -> "Model Context Protocol (MCP) is disabled.");
        }
    }

    /// The server is started in the constructor of [TgMcpServer].
    ///
    @Inject
    static void startMcpServer(final TgMcpServer server) {}

    private Optional<String> findProperty(final String key, final PropertySource source, final PropertySource... sources) {
        return findProperty(key, prepend(source, Arrays.asList(sources)));
    }

    private String getProperty(final String key, final PropertySource source, final PropertySource... sources) {
        return getProperty(key, prepend(source, Arrays.asList(sources)));
    }

    private String getProperty(final String key, final Collection<PropertySource> sources) {
        return findProperty(key, sources)
                .orElseThrow(() -> new MissingParameterDependencyException(format(
                        "Configuration parameter [%s] cannot be empty. Scanned these sources: %s.",
                        key, CollectionUtil.toString(sources, PropertySource::desc, ", "))));
    }

    private Optional<String> findProperty(final String key, final Collection<PropertySource> sources) {
        if (sources.isEmpty()) {
            throw new InvalidArgumentException("At least one property source must be given.");
        }

        return sources.stream()
                .flatMap(src -> maybePropertyFrom(key, src).stream())
                .findFirst();
    }

    private String getPropertyWithDefault(final String key, final String defaultValue, final PropertySource source, final PropertySource... sources) {
        return getPropertyWithDefault(key, defaultValue, prepend(source, Arrays.asList(sources)));
    }

    private String getPropertyWithDefault(final String key, final String defaultValue, final Collection<PropertySource> sources) {
        if (sources.isEmpty()) {
            throw new InvalidArgumentException("At least one property source must be given.");
        }

        return sources.stream()
                .flatMap(src -> maybePropertyFrom(key, src).stream())
                .findFirst()
                .orElse(defaultValue);
    }

    private Optional<String> maybePropertyFrom(final String key, final PropertySource source) {
        final var val = switch (source) {
            case APPLICATION -> properties.getProperty(key);
            case SYSTEM -> System.getProperty(key);
            case ENV -> System.getenv(key);
        };
        return isEmpty(val) ? Optional.empty() : Optional.of(val);
    }

    private enum PropertySource {
        APPLICATION("Application properties"),
        SYSTEM("System properties"),
        ENV("Environment variables");

        public final String desc;

        PropertySource(final String desc) {
            this.desc = desc;
        }

        public String desc() {
            return desc;
        }
    }


}
