package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.UserRoleAssociationResource;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation that provides logic for correct instantiation of {@link UserRoleAssociationResource}.
 *
 * @author TG Team
 *
 */
public class UserRoleAssociationResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;

    /**
     * Principle constructor.
     */
    public UserRoleAssociationResourceFactory(final Injector injector) {
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST.equals(request.getMethod())) {
            final IUserEx controller = injector.getInstance(IUserEx.class);
            final IUserRoleDao userRoleDao = injector.getInstance(IUserRoleDao.class);

            final String username = (String) request.getAttributes().get("username");
            injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserEx.class));

            new UserRoleAssociationResource(controller, userRoleDao, restUtil, getContext(), request, response).handle();
        }
    }
}