package ua.com.fielden.platform.web;

import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import ua.com.fielden.platform.dao.IEntityAggregatesDao2;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.resources.EntityAggregatesQueryExportResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation that provides logic for instantiation of EntityAggregatesQueryExportResource.
 *
 * @author TG Team
 *
 */
public class EntityAggregatesQueryExportResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;

    /**
     * Instances of DAO and factory should be thread-safe as they are used by multiple instances of resources serving concurrent requests.
     *
     * @param dao
     * @param factory
     */
    public EntityAggregatesQueryExportResourceFactory(final Injector injector, final EntityFactory factory) {
	this.injector = injector;
	this.restUtil = new RestServerUtil(injector.getInstance(ISerialiser.class));
    }

    @Override
    public void handle(final Request request, final Response response) {
	super.handle(request, response);

	final IEntityAggregatesDao2 dao = injector.getInstance(IEntityAggregatesDao2.class);

	if (Method.POST.equals(request.getMethod())) {
	    new EntityAggregatesQueryExportResource(dao, restUtil, getContext(), request, response).handlePost();
	}
    }
}
