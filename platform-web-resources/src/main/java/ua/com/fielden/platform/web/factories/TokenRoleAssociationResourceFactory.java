package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.TokenRoleAssociationResource;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation that provides logic for correct instantiation of {@link TokenRoleAssociationResource}.
 *
 * @author TG Team
 *
 */
public class TokenRoleAssociationResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;

    /**
     * Principle constructor.
     *
     * @param dao
     * @param factory
     */
    public TokenRoleAssociationResourceFactory(final Injector injector) {
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        final ISecurityTokenController controller = injector.getInstance(ISecurityTokenController.class);

        final String username = (String) request.getAttributes().get("username");
        injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserEx.class));

        if (Method.GET.equals(request.getMethod())) {
            new TokenRoleAssociationResource(controller, restUtil, getContext(), request, response).handle();
        }
    }
}
