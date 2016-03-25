package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.dao.IComputationMonitor;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.resources.CompanionResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

public class CompanionResourceFactory extends Restlet {
    private final IComputationMonitor resource;
    private final Injector injector;
    private final RestServerUtil restUtil;

    public CompanionResourceFactory(final IComputationMonitor resource, final Injector injector) {
        this.resource = resource;
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        final String username = (String) request.getAttributes().get("username");
        injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUser.class));

        if (Method.GET == request.getMethod() || Method.HEAD == request.getMethod() || Method.POST == request.getMethod() || Method.DELETE == request.getMethod()) {
            final CompanionResource coResource = new CompanionResource(resource, restUtil, getContext(), request, response);
            coResource.handle();
        }
    }
}
