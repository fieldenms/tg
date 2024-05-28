package ua.com.fielden.platform.web.proxy;

import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.util.ProcessorUtils;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import ua.com.fielden.platform.utils.CollectionUtil;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

/**
 * A reverse proxy servlet for local (internal) dispatch between different components of TG-based applications, running as separate Jetty instances, listening on different ports.
 * The original intent was to dispatch request to the main application server (Jetty + Restlet) and an SSE server (Server-Sent Eventing with Jetty).
 * <p>
 * However, it should be relatively easy to extend this servlet for proxying requests to 3rd party services, such as Google Places.
 * This would be useful in situations where Web apps make requests to the app server and the app server rewrites the request by providing additional parameters, such as an API key.
 *
 * @author TG Team
 */
public class LocalReverseProxyServlet extends HttpServlet {

    private static final Logger LOGGER = getLogger(LocalReverseProxyServlet.class);

    // Hop-by-hop headers should be skipped from forwarding for security reasons.
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers#hop-by-hop_headers
    // https://book.hacktricks.xyz/pentesting-web/abusing-hop-by-hop-headers
    // https://nathandavison.com/blog/abusing-http-hop-by-hop-request-headers
    private static final Set<String> HOP_HEADERS = setOf("proxy-connection", "connection", "keep-alive", "transfer-encoding", "te", "trailer", "proxy-authorization", "proxy-authenticate","upgrade");

    private HttpClient httpClient;
    private final String targetServer;
    private final Optional<String> maybeTargetSseServer;
    private final int clientMinThreads, clientMaxThreads, clientIdleTimeout;

    public static Optional<Server> createProxyService(
            final Properties props,
            final Logger logger
    ) {
        if (!Boolean.valueOf(props.getProperty("proxy.enabled", "true"))) {
            logger.warn("Proxy service is disabled. Make sure HAProxy or other external proxy is in use.");
            return empty();
        }
        final int port = parseInt(props.getProperty("proxy.jetty.port", "8090"));
        final int targetPort = parseInt(props.getProperty("port.listen", "Missing value for application property `port.listen`."));
        final int targetSsePort = !Boolean.valueOf(props.getProperty("sse.enabled", "false")) ? 0 : parseInt(props.getProperty("sse.jetty.port", "0"));
        final int minThreads = parseInt(props.getProperty("proxy.jetty.threadPool.minThreads", "5"));
        final int maxThreads = parseInt(props.getProperty("proxy.jetty.threadPool.maxThreads", "50"));
        final int idleTimeout = parseInt(props.getProperty("proxy.jetty.threadPool.idleTimeout", "30000"));
        final int acceptors = parseInt(props.getProperty("proxy.jetty.connector.acceptors", "2"));

        final String targetServer = "http://localhost:" + targetPort;
        final Optional<String> maybeTargetSseServer = targetSsePort == 0 ? empty() : of("http://localhost:" + targetSsePort);

        logger.info(
                """
                Creating Proxy service:
                    proxy.jetty.port..............................%s
                    proxy.jetty.threadPool.minThreads.............%s
                    proxy.jetty.threadPool.maxThreads.............%s
                    proxy.jetty.threadPool.idleTimeout............%s
                    proxy.jetty.connector.acceptors...............%s
                    target server.................................%s
                    target SSE server.............................%s
                """.formatted(port, minThreads, maxThreads, idleTimeout, acceptors, targetServer, maybeTargetSseServer.orElse("disabled")));

        final QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        threadPool.setName("Local-Proxy-Server");
        final Server server = new Server(threadPool);

        final ServerConnector http = new ServerConnector(server, acceptors /* acceptors */, -1 /* selectors */);
        http.setPort(port);
        server.addConnector(http);

        // so we need to add a handler
        final ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        // Proxy servlet instantiation, which still needs to be associated with a Jetty instance
        addProxyServlet(handler, minThreads, maxThreads, idleTimeout, targetServer, maybeTargetSseServer);
        return of(server);
    }


    /**
     * A factory method for instantiating and adding Proxy servlet to {@code handler}.
     *
     * @param handler
     * @param clientMinThreads
     */
    private static void addProxyServlet(
            final ServletHandler handler,
            final int clientMinThreads,
            final int clientMaxThreads,
            final int clientIdleTimeout,
            final String targetServer,
            final Optional<String> maybeTargetSseServer) {
        // Instantiate this servlet.
        final LocalReverseProxyServlet proxyServlet = new LocalReverseProxyServlet(targetServer, maybeTargetSseServer, clientMinThreads, clientMaxThreads, clientIdleTimeout);
        // Configure a servlet holder with async support.
        final ServletHolder servletHolder = new ServletHolder(proxyServlet);
        servletHolder.setAsyncSupported(true);

        // Let's now bind the servlet to the root in order to proxy all requests.
        handler.addServletWithMapping(servletHolder, "/*");
    }

    protected LocalReverseProxyServlet(final String targetServer, final Optional<String> maybeTargetSseServer, final int clientMinThreads, final int clientMaxThreads, final int clientIdleTimeout) {
        this.targetServer = targetServer;
        this.maybeTargetSseServer = maybeTargetSseServer;
        this.clientMinThreads = clientMinThreads;
        this.clientMaxThreads = clientMaxThreads;
        this.clientIdleTimeout = clientIdleTimeout;
    }

    @Override
    public void init() throws ServletException {
        httpClient = newHttpClient();
    }

    /**
     * Creates a new {@link HttpClient} for making proxy requests to the target servers.
     * Instances of {@link HttpClient} are thread-safe and can be reused in different threads, handling incoming requests.
     *
     * @return
     */
    protected HttpClient newHttpClient() throws ServletException {
        final int selectors;
        final String value = this.getServletConfig().getInitParameter("selectors");
        if (value != null) {
            selectors = Integer.parseInt(value);
        }
        else {
            selectors = Math.max(1, ProcessorUtils.availableProcessors() / 2);
        }

        // This reverse proxy is designed for embedded application use and is not equipped to handle SSL.
        // It is expected that an external reverse proxy, such as HAProxy or Envoy, would be placed in front of an application for ingres.
        // Therefore, it should be safe to trust all HTTPS traffic.
        final SslContextFactory.Client clientSsl = new SslContextFactory.Client(true);
        final var httpClient = new HttpClient(new HttpClientTransportOverHTTP(selectors), clientSsl);
        // HttpClient handles responses that are redirects automatically by default.
        // For the purpose of proxying, such redirects need to be passed back to the requester as is, without performing redirects internally.
        // Disable automatic redirects.
        httpClient.setFollowRedirects(false);

        // Cookies should not be stored to avoid accumulation of cookies from different proxied requests.
        httpClient.setCookieStore(new HttpCookieStore.Empty());

        // Configure an executor for the HTTP client
        final QueuedThreadPool threadPool = new QueuedThreadPool(clientMaxThreads, clientMinThreads, clientIdleTimeout);
        threadPool.setName("Local-Proxy-Client");
        httpClient.setExecutor(threadPool);

        try {
            httpClient.start();
            // HttpClient is used for making proxied requests, where the content of the response is then passed back to the original requester.
            // The response content should be passed back without any changes. However, by default HttpClient tries to decode any encoded content (e.g., if it is gzipped, it would get unzipped).
            // Content decoding needs to be avoided. This can only be done by removing content decoder factories, which should be performed after the start of HttpClient, where default decoders are assigned.
            // Disable automatic decoding.
            httpClient.getContentDecoderFactories().clear();
            return httpClient;
        } catch (final Exception ex) {
            final String msg = "Proxy Server failed to start HttpClient.";
            LOGGER.error(msg, ex);
            throw new ServletException(msg, ex);
        }
    }

    /**
     * Handles proxying for incoming requests, using {@link AsyncContext} for the best performance.
     * The main idea of proxying is to make a new request based on the original request, with a new destination and then copy the thus proxied response (including headers) in response to that original request.
     * Special care is taken to correctly process proxied responses, which are redirects.
     *
     * @param req   the {@link HttpServletRequest} object that
     *                  contains the request the client made of
     *                  the servlet
     *
     * @param resp  the {@link HttpServletResponse} object that
     *                  contains the response the servlet returns
     *                  to the client
     *
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // Server-Sent Event requests require special handling:
        // 1. Forward to a designated resource, if specified.
        // 2. Async operation should not timeout or complete, and aggressive flushing of the content buffer is required to push all data back to the client.
        final boolean isSSE = "text/event-stream".equals(req.getHeader("Accept"));
        final String requestTargetServer = isSSE
                                           ? maybeTargetSseServer.orElse(targetServer)
                                           : targetServer;
        final String targetUrl;
        try {
            final String queryString = req.getQueryString();
            URI uri = new URI(requestTargetServer + req.getRequestURI() + (queryString == null ? "" : "?" + queryString));
            targetUrl = uri.toString();
        } catch (final URISyntaxException ex) {
            final String msg = "Invalid URI: " + ex.getMessage();
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
            LOGGER.error(msg, ex);
            return;
        }

        // Use AsyncContext to improve performance
        final AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(isSSE ? 0 : 20 * 60_000); // TODO  Set a timeout for the async context as a configuration property.

        // Create proxy request with targetUrl as the destination
        final Request proxyRequest = httpClient.newRequest(targetUrl).method(req.getMethod());
        // proxyRequest.timeout(60_000, TimeUnit.MILLISECONDS); // TODO Timeout for the client needs to be considered further, including extra testing.


        // Copy request headers from the original request, skipping hop-by-hop headers.
        req.getHeaderNames().asIterator()
                            .forEachRemaining(headerName -> {
                                if (!HOP_HEADERS.contains(headerName.toLowerCase())) {
                                    req.getHeaders(headerName).asIterator().forEachRemaining(headerValue -> {
                                        proxyRequest.header(headerName, headerValue);
                                    });
                                }
                            });
        // Add X-Forwarded headers
        final String originalForwardedFor = req.getHeader("X-Forwarded-For");
        final String clientIp = req.getRemoteAddr();
        if (originalForwardedFor != null) {
            proxyRequest.header("X-Forwarded-For", originalForwardedFor + ", " + clientIp);
        } else {
            proxyRequest.header("X-Forwarded-For", clientIp);
        }
        proxyRequest.header("X-Forwarded-Host", req.getHeader("Host"));
        proxyRequest.header("X-Forwarded-Proto", req.getScheme());

        // Copy the request body if present
        if (req.getContentLength() > 0) {
            final ByteArrayOutputStream requestBodyStream = new ByteArrayOutputStream();
            req.getInputStream().transferTo(requestBodyStream);
            byte[] requestBody = requestBodyStream.toByteArray();
            proxyRequest.content(new BytesContentProvider(requestBody));
        }

        // Send the proxy request asynchronously and provide a response listener to handle the proxied response.
        proxyRequest.send(new Response.Listener.Adapter() {

            @Override
            public void onHeaders(final Response response) {
                HttpServletResponse proxyResponse = (HttpServletResponse) asyncContext.getResponse();
                proxyResponse.setStatus(response.getStatus());
                response.getHeaders().forEach(header -> proxyResponse.setHeader(header.getName(), header.getValue()));
            }

            @Override
            public void onContent(final Response response, final ByteBuffer content) {
                HttpServletResponse proxyResponse = (HttpServletResponse) asyncContext.getResponse();
                try {
                    final byte[] bytes = new byte[content.remaining()];
                    content.get(bytes);
                    proxyResponse.getOutputStream().write(bytes);
                    // Immediately flush content for SSE requests, which critical for the heartbeat to work correctly.
                    if (isSSE) {
                        proxyResponse.getOutputStream().flush();
                    }
                } catch (final IOException ex) {
                    asyncContext.complete();
                    LOGGER.error("Proxy Server content IO error.", ex);
                }
            }

            @Override
            public void onComplete(final Result result) {
                if (result.isFailed()) {
                    final HttpServletResponse proxyResponse = (HttpServletResponse) asyncContext.getResponse();
                    final String msg = "Proxy error (on complete): " + result.getFailure().getMessage();
                    try {
                        LOGGER.info(msg);
                        proxyResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
                    } catch (final IOException ex) {
                        LOGGER.error("Proxy Server could not send error [%s].".formatted(msg), ex);
                    } finally {
                        asyncContext.complete();
                    }
                }
                else {
                    final Response response = result.getResponse();
                    if (response.getStatus() >= 300 && response.getStatus() < 400) {
                        final String location = response.getHeaders().get("Location");
                        if (location != null) {
                            // Rewrite the location header to ensure correct redirection.
                            final String newLocation = location.startsWith(requestTargetServer)
                                                       ? location.replace(requestTargetServer, req.getRequestURL().substring(0, req.getRequestURL().indexOf(req.getRequestURI())))
                                                       : location;
                            final HttpServletResponse proxyResponse = (HttpServletResponse) asyncContext.getResponse();
                            proxyResponse.setHeader("Location", newLocation);
                            asyncContext.complete();
                        }
                    }
                    else {
                        // The context should not be completed for SSE requests as it will be reused.
                        if (!isSSE) {
                            asyncContext.complete();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void destroy() {
        try {
            httpClient.stop();
        } catch (final Exception ex) {
            LOGGER.error("Proxy Server encountered an error during stopping.", ex);
        }
    }

}
