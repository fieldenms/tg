package ua.com.fielden.platform.web;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.snappy.ISnappyDao;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.SnappyQueryResource;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation that provides logic for correct snappy query oriented resource instantiation. Specifically, it should be used to instantiate
 * {@link SnappyQueryResource}.
 * 
 * @author Jhou
 * 
 */
public class SnappyQueryRestlet<T extends AbstractEntity> extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;

    /**
     * Instances of factory should be thread-safe as they are used by multiple instances of resources serving concurrent requests.
     */
    public SnappyQueryRestlet(final Injector injector) {
        this.injector = injector;
        this.restUtil = new RestServerUtil(injector.getInstance(ISerialiser.class));
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        final ISnappyDao dao = injector.getInstance(ISnappyDao.class);
        final String username = (String) request.getAttributes().get("username");
        injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserController.class));

        if (Method.GET.equals(request.getMethod())) {
        } else if (Method.HEAD.equals(request.getMethod())) {
        } else if (Method.PUT.equals(request.getMethod())) {
        } else if (Method.POST.equals(request.getMethod())) {
            new SnappyQueryResource<T>(dao, restUtil, getContext(), request, response).handle();
        }
    }
}
