package ua.com.fielden.platform.web;

import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.resources.EntityInstanceResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation that provides logic for correct entity oriented resource instantiation.
 * Specifically, it should be used to instantiate {@link EntityInstanceResource} for specific entity types.
 *
 * @author 01es
 *
 */
public class EntityInstanceResourceFactory<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends Restlet {
    private final Class<DAO> daoType;
    private final Injector injector;
    private final EntityFactory factory;
    private final RestServerUtil restUtil;

    /**
     * Instances of DAO and factory should be thread-safe as they are used by multiple instances of resources serving concurrent requests.
     */
    public EntityInstanceResourceFactory(final Class<DAO> daoType, final Injector injector, final EntityFactory factory) {
	this.daoType = daoType;
	this.injector = injector;
	this.factory = factory;
	this.restUtil = new RestServerUtil(injector.getInstance(ISerialiser.class));
    }

    @Override
    public void handle(final Request request, final Response response) {
	super.handle(request, response);

	final DAO dao = injector.getInstance(daoType);

	final EntityInstanceResource<T> resource = new EntityInstanceResource<T>(dao, factory, restUtil, getContext(), request, response);
	if (Method.GET == request.getMethod()) {
	    resource.handleGet();
	} else if (Method.HEAD == request.getMethod()) {
	    resource.handleHead();
	} else if (Method.POST == request.getMethod()) {
	    resource.handlePost();
	} else if (Method.DELETE == request.getMethod()) {
	    resource.handleDelete();
	}
    }
}
