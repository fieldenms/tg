package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.web.resources.FileProcessingResource;

/**
 * Factory to instantiate {@link FileProcessingResource}.
 *
 * @author TG Team
 *
 */
public class FileProcessingResourceFactory extends Restlet {
    private final Injector injector;

    public FileProcessingResourceFactory(final Injector injector) {
        this.injector = injector;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.PUT.equals(request.getMethod())) {
            new FileProcessingResource(injector, getContext(), request, response).handle();
        }
    }
}
