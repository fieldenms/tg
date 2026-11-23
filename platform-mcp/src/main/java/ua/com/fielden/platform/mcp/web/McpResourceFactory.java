package ua.com.fielden.platform.mcp.web;

import io.modelcontextprotocol.server.McpStatelessServerHandler;
import io.modelcontextprotocol.spec.McpStatelessServerTransport;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import reactor.core.publisher.Mono;
import ua.com.fielden.platform.mcp.TgMcpServer;

/// Web server resource for the MCP Server.
///
/// This factory must be a singleton to make it usable as a [transport][McpStatelessServerTransport] for the [MCP server][TgMcpServer].
///
@Singleton
public class McpResourceFactory extends Restlet implements McpStatelessServerTransport {

    private final McpResource.Factory mcpResourceFactory;
    private McpStatelessServerHandler mcpHandler;
    private volatile boolean isClosing = false;

    @Inject
    protected McpResourceFactory(final McpResource.Factory mcpResourceFactory) {
        this.mcpResourceFactory = mcpResourceFactory;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (isClosing) {
            response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, "Server is shutting down");
            return;
        }

        mcpResourceFactory.create(getContext(), request, response, mcpHandler).handle();
    }

    @Override
    public void setMcpHandler(final McpStatelessServerHandler mcpHandler) {
        this.mcpHandler = mcpHandler;
    }

    @Override
    public Mono<Void> closeGracefully() {
        return Mono.fromRunnable(() -> this.isClosing = true);
    }

}