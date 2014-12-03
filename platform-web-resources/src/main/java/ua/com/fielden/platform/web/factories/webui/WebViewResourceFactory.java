package ua.com.fielden.platform.web.factories.webui;

import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.resources.webui.WebViewResource;
import ua.com.fielden.platform.web.view.AbstractWebView;

/**
 * The server resource factory for entity centres;
 *
 * @author TG Team
 *
 */
public class WebViewResourceFactory extends Restlet {
    private final Map<String, AbstractWebView<?>> webViews;

    /**
     * Creates the {@link WebViewResourceFactory} instance with map of available web models.
     *
     */
    public WebViewResourceFactory(final Map<String, AbstractWebView<?>> webViews) {
        this.webViews = webViews;
    }

    @Override
    /**
     * Invokes on GET request from client.
     */
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            final String webViewName = request.getAttributes().get("webViewName").toString();
            new WebViewResource(webViews.get(webViewName), getContext(), request, response).handle();
        }
    }
}
