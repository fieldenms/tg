package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CentreComponentResource;

/**
 * The server resource factory for entity centres.
 *
 * The centre identification information is a part of the URI: "/centre_ui/{mitype}".
 *
 * @author TG Team
 *
 */
public class CentreComponentResourceFactory extends Restlet {
    private final ISourceController sourceController;
    private final RestServerUtil restUtil;
    private final IUserProvider userProvider;

    /**
     * Creates the {@link CentreComponentResourceFactory} instance.
     *
     * @param centres
     */
    public CentreComponentResourceFactory(final ISourceController sourceController, final RestServerUtil restUtil, final IUserProvider userProvider) {
        this.sourceController = sourceController;
        this.restUtil = restUtil;
        this.userProvider = userProvider;
    }

    /**
     * Invokes on GET request from client.
     */
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new CentreComponentResource(
                    sourceController,
                    restUtil,
                    userProvider,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}
