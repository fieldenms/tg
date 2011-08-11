package ua.com.fielden.platform.web;

import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.SecurityTokenResource;

import com.google.inject.Injector;

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
	this.restUtil = new RestServerUtil(injector.getInstance(ISerialiser.class));
    }

    @Override
    public void handle(final Request request, final Response response) {
	super.handle(request, response);

	final ISecurityTokenController controller = injector.getInstance(ISecurityTokenController.class);
	final IUserRoleDao userRoleDao = injector.getInstance(IUserRoleDao.class);

	if (Method.GET.equals(request.getMethod())) {
	    new SecurityTokenResource(controller, userRoleDao, restUtil, getContext(), request, response).handleGet();
	} else if (Method.POST.equals(request.getMethod())) {
	    new SecurityTokenResource(controller, userRoleDao, restUtil, getContext(), request, response).handlePost();
	} else if (Method.HEAD.equals(request.getMethod())) {
	    new SecurityTokenResource(controller, userRoleDao, restUtil, getContext(), request, response).handleHead();
	}
    }
}
