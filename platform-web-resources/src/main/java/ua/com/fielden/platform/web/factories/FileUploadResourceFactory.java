package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.resources.FileUploadResource;

/**
 * Factory to instantiate {@link FileUploadResource}.
 *
 * @author TG Team
 *
 */
public class FileUploadResourceFactory extends Restlet {
    private final Injector injector;

    public FileUploadResourceFactory(final Injector injector) {
        this.injector = injector;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        final String username = (String) request.getAttributes().get("username");
        injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserEx.class));
        if (Method.PUT.equals(request.getMethod())) {
            new FileUploadResource(injector, getContext(), request, response).handle();
        }
    }
}
