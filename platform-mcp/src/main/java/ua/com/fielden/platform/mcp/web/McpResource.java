package ua.com.fielden.platform.mcp.web;

import com.google.inject.assistedinject.Assisted;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpStatelessServerHandler;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCNotification;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCRequest;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.webui.AbstractWebResource;

import java.io.IOException;

/// Web server resource for the MCP Server.
///
public class McpResource extends AbstractWebResource {

    private static final Logger LOGGER = LogManager.getLogger();

    private final McpStatelessServerHandler mcpHandler;

    @Inject
    protected McpResource(
            final IDeviceProvider deviceProvider,
            final IDates dates,
            @Assisted @Nullable final Context context,
            @Assisted @Nullable final Request request,
            @Assisted @Nullable final Response response,
            @Assisted final McpStatelessServerHandler mcpHandler)
    {
        super(context, request, response, deviceProvider, dates);
        this.mcpHandler = mcpHandler;
    }

    public interface Factory {
        McpResource create(Context context, Request request, Response response, McpStatelessServerHandler mcpHandler);
    }

    @Post
    public Representation post(final @Nullable Representation envelope) {
        if (envelope == null) {
            return errorResponse(Status.CLIENT_ERROR_BAD_REQUEST,
                                 McpError.builder(McpSchema.ErrorCodes.INVALID_REQUEST)
                                         .message("The request must not be empty")
                                         .build());
        }

        try {
            final var body = envelope.getText();
            final var message = McpSchema.deserializeJsonRpcMessage(McpJsonDefaults.getMapper(), body);

            return switch (message) {
                case JSONRPCRequest request -> handleRequest(request);
                case JSONRPCNotification notification -> handleNotification(notification);
                case null, default ->
                        errorResponse(Status.CLIENT_ERROR_BAD_REQUEST,
                                      McpError.builder(McpSchema.ErrorCodes.INVALID_REQUEST)
                                              .message("The server accepts either requests or notifications")
                                              .build());
            };
        } catch (final IllegalArgumentException | IOException e) {
            LOGGER.error("Deserialisation failed.", e);
            return errorResponse(Status.CLIENT_ERROR_BAD_REQUEST,
                                 McpError.builder(McpSchema.ErrorCodes.INVALID_REQUEST)
                                         .message(e.getMessage())
                                         .build());
        } catch (final Exception e) {
            LOGGER.error("Unexpected error.", e);
            return errorResponse(Status.SERVER_ERROR_INTERNAL,
                                 McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR)
                                         .message("Unexpected error: " + e.getMessage())
                                         .build());
        }
    }

    @Get
    public Representation get() {
        getResponse().setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        return new EmptyRepresentation();
    }

    private Representation handleRequest(final JSONRPCRequest jsonrpcRequest) {
        try {
            final var transportContext = McpTransportContext.EMPTY;
            final JSONRPCResponse jsonrpcResponse = mcpHandler
                    .handleRequest(transportContext, jsonrpcRequest)
                    .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
                    .block();
            return new StringRepresentation(McpJsonDefaults.getMapper().writeValueAsString(jsonrpcResponse), MediaType.APPLICATION_JSON);
        } catch (final Exception e) {
            LOGGER.error("Failed to handle request.\n%s".formatted(jsonrpcRequest), e);
            return errorResponse(Status.SERVER_ERROR_INTERNAL,
                                 McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR)
                                         .message("Failed to handle request: " + e.getMessage())
                                         .build());
        }
    }

    private Representation handleNotification(final JSONRPCNotification jsonrpcNotification) {
        try {
            final var transportContext = McpTransportContext.EMPTY;
            mcpHandler.handleNotification(transportContext, jsonrpcNotification)
                    .contextWrite(ctx -> ctx.put(McpTransportContext.KEY, transportContext))
                    .block();
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            return new EmptyRepresentation();
        } catch (final Exception e) {
            LOGGER.error("Failed to handle notification.\n%s".formatted(jsonrpcNotification), e);
            return errorResponse(Status.SERVER_ERROR_INTERNAL,
                                 McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR)
                                         .message("Failed to handle notification: " + e.getMessage())
                                         .build());
        }
    }

    /// Sends an error response to the client.
    ///
    private Representation errorResponse(final Status status, final McpError mcpError) {
        getResponse().setStatus(status);
        try {
            return new StringRepresentation(McpJsonDefaults.getMapper().writeValueAsString(mcpError), MediaType.APPLICATION_JSON);
        } catch (final IOException e) {
            LOGGER.error("Failed to serialize error response.\n%s".formatted(mcpError), e);
            return new StringRepresentation("{\"error\": \"Internal server error\"}", MediaType.APPLICATION_JSON);
        }
    }

}
