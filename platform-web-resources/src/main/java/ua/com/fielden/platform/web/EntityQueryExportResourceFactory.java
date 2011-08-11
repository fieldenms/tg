package ua.com.fielden.platform.web;

import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.resources.EntityQueryExportResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation that provides logic for correct entity export resource instantiation. Specifically, it should be used to instantiate
 * {@link EntityQueryExportResource} for specific entity types.
 *
 * @author TG Team
 *
 */
public class EntityQueryExportResourceFactory<T extends AbstractEntity, DAO extends IEntityDao<T>> extends Restlet {
    private final Class<DAO> daoType;
    private final Injector injector;
    private final RestServerUtil restUtil;

    /**
     * Instances of DAO and factory should be thread-safe as they are used by multiple instances of resources serving concurrent requests.
     */
    public EntityQueryExportResourceFactory(final Class<DAO> daoType, final Injector injector) {
	this.daoType = daoType;
	this.injector = injector;
	this.restUtil = new RestServerUtil(injector.getInstance(ISerialiser.class));
    }

    @Override
    public void handle(final Request request, final Response response) {
	super.handle(request, response);

	final DAO dao = injector.getInstance(daoType);

	if (Method.POST.equals(request.getMethod())) {
	    new EntityQueryExportResource<T>(dao, restUtil, getContext(), request, response).handlePost();
	}
    }
}
