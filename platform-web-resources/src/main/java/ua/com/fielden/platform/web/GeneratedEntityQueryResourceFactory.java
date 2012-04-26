package ua.com.fielden.platform.web;

import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import ua.com.fielden.platform.dao.DynamicEntityDao;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.resources.GeneratedEntityQueryResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation that provides logic for correct generated entity query resource instantiation. Specifically, it should be used to instantiate
 * {@link GeneratedEntityQueryResource}.
 *
 * @author TG Team
 *
 */
public class GeneratedEntityQueryResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;

    public GeneratedEntityQueryResourceFactory(final Injector injector) {
	this.injector = injector;
	this.restUtil = new RestServerUtil(injector.getInstance(ISerialiser.class));
    }

    @Override
    public void handle(final Request request, final Response response) {
	super.handle(request, response);

	if (Method.POST.equals(request.getMethod())) {
	    final DynamicEntityDao dao = injector.getInstance(DynamicEntityDao.class);
	    new GeneratedEntityQueryResource(dao, restUtil, getContext(), request, response).handlePost();
	}
    }
}
