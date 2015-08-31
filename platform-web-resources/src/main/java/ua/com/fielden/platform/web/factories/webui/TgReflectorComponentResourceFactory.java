package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.resources.webui.TgReflectorComponentResource;

/**
 * Resource factory for tg-reflector component.
 *
 * @author TG Team
 *
 */
public class TgReflectorComponentResourceFactory extends Restlet {
    private final ISourceController sourceController;

    public TgReflectorComponentResourceFactory(final Injector injector) {
        this.sourceController = injector.getInstance(ISourceController.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod()) {
            final TgReflectorComponentResource resource = new TgReflectorComponentResource(sourceController, getContext(), request, response);
            resource.handle();
        }
    }
}
