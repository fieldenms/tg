package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.SecurityTokenResource;

/**
 * This is {@link Restlet} implementation that provides logic for correct instantiation of {@link SecurityTokenResource}.
 *
 * @author TG Team
 *
 */
public class SecurityTokenResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;

    /**
     * Principle constructor.
     *
     * @param dao
     * @param factory
     */
    public SecurityTokenResourceFactory(final Injector injector) {
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        final ISecurityTokenController controller = injector.getInstance(ISecurityTokenController.class);
        final IUserRoleDao userRoleDao = injector.getInstance(IUserRoleDao.class);
        final IUser coUser = injector.getInstance(IUser.class);

        final String username = (String) request.getAttributes().get("username");
        injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUser.class));

        if (Method.GET.equals(request.getMethod()) || Method.POST.equals(request.getMethod()) || Method.HEAD.equals(request.getMethod())) {
            new SecurityTokenResource(controller, userRoleDao, coUser, restUtil, getContext(), request, response).handle();
        }
    }
}
