package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.TgElementLoaderComponentResource;

/**
 * Resource factory for tg-element-loader component.
 *
 * @author TG Team
 *
 */
public class TgElementLoaderComponentResourceFactory extends Restlet {
    private final RestServerUtil restUtil;

    public TgElementLoaderComponentResourceFactory(final Injector injector) {
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod()) {
            final TgElementLoaderComponentResource resource = new TgElementLoaderComponentResource(restUtil, getContext(), request, response);
            resource.handle();
        }
    }
}
