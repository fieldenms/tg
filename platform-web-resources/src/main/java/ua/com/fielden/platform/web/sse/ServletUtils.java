package ua.com.fielden.platform.web.sse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.adapter.Call;
import org.restlet.engine.adapter.HttpRequest;
import org.restlet.engine.adapter.HttpResponse;
import org.restlet.ext.jetty.internal.JettyServerCall;
import org.restlet.ext.servlet.internal.ServletCall;

/**
 *
 * This code amends the original implementation by Jerome to support Jetty connector.
 *
 * @author TG Team
 * @author Jerome Louvel
 *
 */
public final class ServletUtils {

    /**
     * Returns the Servlet request that was used to generate the given Restlet request.
     *
     * @param request
     *            The Restlet request.
     * @return The Servlet request or null.
     */
    public static HttpServletRequest getRequest(final Request request) {
        HttpServletRequest result = null;

        if (request instanceof HttpRequest) {
            final Call call = ((HttpRequest) request).getHttpCall();

            if (call instanceof ServletCall) {
                result = ((ServletCall) call).getRequest();
            } else if (call instanceof JettyServerCall) {
                result = ((JettyServerCall) call).getChannel().getRequest();
            }
        }

        return result;
    }

    /**
     * Returns the Servlet response that was used to generate the given Restlet response.
     *
     * @param response
     *            The Restlet response.
     * @return The Servlet request or null.
     */
    public static HttpServletResponse getResponse(final Response response) {
        HttpServletResponse result = null;

        if (response instanceof HttpResponse) {
            final Call call = ((HttpResponse) response).getHttpCall();

            if (call instanceof ServletCall) {
                result = ((ServletCall) call).getResponse();
            } else if (call instanceof JettyServerCall) {
                result = ((JettyServerCall) call).getChannel().getResponse();
            }
        }

        return result;
    }

}
