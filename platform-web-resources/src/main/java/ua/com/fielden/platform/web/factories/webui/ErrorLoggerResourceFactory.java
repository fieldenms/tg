package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.webui.ErrorLoggerResource;

/**
 * Resource factory that logs client side errors.
 *
 * @author TG Team
 *
 */
public class ErrorLoggerResourceFactory extends Restlet {

    private final IUserProvider userProvider;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;

    public ErrorLoggerResourceFactory(final Injector injector) {
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
        this.dates = injector.getInstance(IDates.class);
        this.userProvider = injector.getInstance(IUserProvider.class);
    }

    /**
     * Handles PUT requests.
     */
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.PUT.equals(request.getMethod())) {
            new ErrorLoggerResource(getContext(),
                    request,
                    response,
                    userProvider,
                    deviceProvider,
                    dates).handle();
        }
    }
}
