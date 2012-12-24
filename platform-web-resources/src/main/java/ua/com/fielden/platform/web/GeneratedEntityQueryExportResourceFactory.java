package ua.com.fielden.platform.web;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.dao.DynamicEntityDao;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.resources.GeneratedEntityQueryExportResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation that provides logic for correct entity export resource instantiation. Specifically, it should be used to instantiate
 * {@link GeneratedEntityQueryExportResource}.
 *
 * @author TG Team
 *
 */
public class GeneratedEntityQueryExportResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;

    public GeneratedEntityQueryExportResourceFactory(final Injector injector) {
	this.injector = injector;
	this.restUtil = new RestServerUtil(injector.getInstance(ISerialiser.class));
    }

    @Override
    public void handle(final Request request, final Response response) {
	super.handle(request, response);


	if (Method.POST.equals(request.getMethod())) {
	    final DynamicEntityDao dao = injector.getInstance(DynamicEntityDao.class);

	    final String username = (String) request.getAttributes().get("username");
	    injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserController.class));

	    new GeneratedEntityQueryExportResource(dao, restUtil, getContext(), request, response).handle();
	}
    }
}
