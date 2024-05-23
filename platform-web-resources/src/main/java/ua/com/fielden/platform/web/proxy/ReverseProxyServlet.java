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
import org.eclipse.jetty.util.ProcessorUtils;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

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

import static java.lang.Integer.parseInt;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * A reverse proxy servlet for internal dispatch between different components of TG-based applications, running as separate Jetty instances, listening on different ports.
 * The original intent was to dispatch request to the main application server (Jetty + Restlet) and an SSE server (Server-Sent Eventing with Jetty).
 * <p>
 * However, it should be relatively easy to extend this servlet for proxying requests to 3rd party services, such as Google Places.
 * This would be useful in situations where Web apps make requests to the app server and the app server rewrites the request by providing additional parameters, such as an API key.
 *
 * @author TG Team
 */
public class ReverseProxyServlet extends HttpServlet {

    private HttpClient httpClient;
    private String targetServer;
    private String targetSseServer;

    public static Optional<Server> createProxyService(
            final Properties props,
            final boolean requiresHttps,
            final Logger logger
    ) {
        if (!Boolean.valueOf(props.getProperty("proxy.enabled", "true"))) {
            logger.warn("Proxy service is disabled. Make sure HAProxy or other external proxy is in use.");
            return empty();
        }
        final int port = parseInt(props.getProperty("proxy.jetty.port", "8090"));
        final int minThreads = parseInt(props.getProperty("proxy.jetty.threadPool.minThreads", "5"));
        final int maxThreads = parseInt(props.getProperty("proxy.jetty.threadPool.maxThreads", "50"));
        final int idleTimeout = parseInt(props.getProperty("proxy.jetty.threadPool.idleTimeout", "30000"));
        final int acceptors = parseInt(props.getProperty("proxy.jetty.connector.acceptors", "2"));

        logger.info(
                """
                Creating Proxy service:
                    proxy.jetty.port..............................%s
                    proxy.jetty.threadPool.maxThreads.............%s
                    proxy.jetty.threadPool.minThreads.............%s
                    proxy.jetty.threadPool.idleTimeout............%s
                    proxy.jetty.connector.acceptors...............%s
                """.formatted(port, maxThreads, minThreads, idleTimeout, acceptors));

        final QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        final Server server = new Server(threadPool);

        final ServerConnector http = new ServerConnector(server, acceptors /* acceptors */, -1 /* selectors */);
        http.setPort(port);
        server.addConnector(http);

        // so we need to add a handler
        final ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        // Proxy servlet instantiation, which still needs to be associated with a Jetty instance
        addProxyServlet(handler, requiresHttps, maxThreads, minThreads, idleTimeout, acceptors);
        return of(server);
    }


    /**
     * A factory method for instantiating and adding Proxy servlet to {@code handler}.
     *
     * @param handler
     * @param requiresHttps
     */
    private static void addProxyServlet(
            final ServletHandler handler,
            final boolean requiresHttps,
            final int maxThreads,
            final int minThreads,
            final int idleTimeout,
            final int acceptors) {
        // Instantiate this servlet.
        final ReverseProxyServlet proxyServlet = new ReverseProxyServlet();
        // Configure a servlet holder with async support.
        final ServletHolder servletHolder = new ServletHolder(proxyServlet);
        servletHolder.setInitParameter("maxThreads", maxThreads + "");
        servletHolder.setInitParameter("minThreads", minThreads + "");
        servletHolder.setInitParameter("idleTimeout", idleTimeout + "");
        servletHolder.setInitParameter("acceptors", acceptors + "");
        servletHolder.setAsyncSupported(true);

        // Let's now bind the servlet to the root in order to proxy all requests.
        handler.addServletWithMapping(servletHolder, "/*");
    }


    @Override
    public void init() throws ServletException {
        // TODO The base URLs of the target servers, which need to be passed as parameters.
        targetServer = "http://localhost:8091";
        targetSseServer = "http://localhost:8092";
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

        try {
            httpClient.start();
            // HttpClient is used for making proxied requests, where the content of the response is then passed back to the original requester.
            // The response content should be passed back without any changes. However, by default HttpClient tries to decode any encoded content (e.g., if it is gzipped, it would get unzipped).
            // Content decoding needs to be avoided. This can only be done by removing content decoder factories, which should be performed after the start of HttpClient, where default decoders are assigned.
            // Disable automatic decoding.
            httpClient.getContentDecoderFactories().clear();
            return httpClient;
        } catch (Exception e) {
            throw new ServletException("Failed to start HttpClient", e);
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
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String requestTargetServer = req.getRequestURI().startsWith("/sse/")
                                           ? targetSseServer
                                           : targetServer;
        final String targetUrl;
        try {
            final String queryString = req.getQueryString();
            URI uri = new URI(requestTargetServer + req.getRequestURI() + (queryString == null ? "" : "?" + queryString));
            targetUrl = uri.toString();
        } catch (final URISyntaxException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URI: " + ex.getMessage());
            return;
        }

        // Use AsyncContext to improve performance
        final AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(30_000); // TODO  Set a timeout for the async context as a configuration property.

        // Create proxy request with targetUrl as the destination
        final Request proxyRequest = httpClient.newRequest(targetUrl).method(req.getMethod());

        // Copy request headers from the original request
        req.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            req.getHeaders(headerName).asIterator().forEachRemaining(headerValue -> {
                proxyRequest.header(headerName, headerValue);
            });
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

        // send the proxy request asynchronously and provide a response listener to handle the proxied response
        proxyRequest.send(new Response.Listener.Adapter() {

            @Override
            public void onHeaders(Response response) {
                HttpServletResponse proxyResponse = (HttpServletResponse) asyncContext.getResponse();
                proxyResponse.setStatus(response.getStatus());
                response.getHeaders().forEach(header -> proxyResponse.setHeader(header.getName(), header.getValue()));
            }

            @Override
            public void onContent(Response response, ByteBuffer content) {
                HttpServletResponse proxyResponse = (HttpServletResponse) asyncContext.getResponse();
                try {
                    byte[] bytes = new byte[content.remaining()];
                    content.get(bytes);
                    proxyResponse.getOutputStream().write(bytes);
                } catch (final IOException ex) {
                    asyncContext.complete();
                }
            }

            @Override
            public void onComplete(Result result) {
                if (result.isFailed()) {
                    final HttpServletResponse proxyResponse = (HttpServletResponse) asyncContext.getResponse();
                    try {
                        proxyResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Proxy Error: " + result.getFailure().getMessage());
                    } catch (final IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    final Response response = result.getResponse();
                    if (response.getStatus() >= 300 && response.getStatus() < 400) {
                        final String location = response.getHeaders().get("Location");
                        if (location != null) {
                            // Rewrite the location header to ensure correct redirection
                            final String newLocation = location.startsWith(requestTargetServer)
                                                       ? location.replace(requestTargetServer, req.getRequestURL().substring(0, req.getRequestURL().indexOf(req.getRequestURI())))
                                                       : location;
                            HttpServletResponse proxyResponse = (HttpServletResponse) asyncContext.getResponse();
                            proxyResponse.setHeader("Location", newLocation);
                        }
                    }
                }
                asyncContext.complete();
            }
        });
    }

    @Override
    public void destroy() {
        try {
            httpClient.stop();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

}
