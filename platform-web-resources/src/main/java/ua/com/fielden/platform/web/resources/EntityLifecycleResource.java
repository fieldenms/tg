package ua.com.fielden.platform.web.resources;

import java.util.List;

import org.joda.time.DateTime;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import ua.com.fielden.platform.dao.ILifecycleDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;

/**
 * Represents a web resource mapped to URI /lifecycle/entity-alias-type. It handles POST requests provided with {@link EntityResultQueryModel} and a "propertyName" with lifecycle "period" to provide the result of the query as lifecycle data.
 * <p>
 * Each request is handled by a new resource instance, thus the only thread-safety requirement is to have provided DAO and entity factory thread-safe.
 *
 * @author TG Team
 */
public class EntityLifecycleResource<T extends AbstractEntity<?>> extends Resource {
    // the following properties are determined from request
    private final String username;

    private final ILifecycleDao<T> lifecycleDao;
    private final RestServerUtil restUtil;

    /**
     * The main resource constructor accepting a LifecycleDAO instance in addition to the standard {@link Resource} parameters.
     *
     * @param dao
     * @param context
     * @param request
     * @param response
     */
    public EntityLifecycleResource(final ILifecycleDao<T> lifecycleDao, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	super(context, request, response);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
	this.lifecycleDao = lifecycleDao;
	this.restUtil = restUtil;
	this.username = (String) request.getAttributes().get("username");
	lifecycleDao.setUsername(username);
    }

    // //////////////////////////////////////////////////////////////////
    // let's specify what HTTP methods are supported by this resource //
    // //////////////////////////////////////////////////////////////////
    @Override
    public boolean allowPost() {
	return true;
    }

    @Override
    public boolean allowGet() {
	return false;
    }

    /**
     * Handles POST request resulting from RAO call. It is expected that envelope is a serialised representation of a list containing {@link IQueryModel}+"propertyName"+"period".
     */
    @Override
    public void acceptRepresentation(final Representation envelope) throws ResourceException {
	try {
	    final List<?> list = restUtil.restoreList(envelope);
	    final SingleResultQueryModel<? extends AbstractEntity<?>> model = (SingleResultQueryModel<? extends AbstractEntity<?>>) list.get(0);
	    final List<byte[]> binaryTypes = (List<byte[]>) list.get(1);
	    final String propertyName = (String) list.get(2);
	    final DateTime from = (DateTime) list.get(3);
	    final DateTime to = (DateTime) list.get(4);

	    //Loading classes for enhanced types.
	    final DynamicEntityClassLoader classLoader = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());
	    for (final byte[] binaryType : binaryTypes) {
		classLoader.defineClass(binaryType);
	    }

	    getResponse().setEntity(restUtil.lifecycleRepresentation(lifecycleDao.getLifecycleInformation(model, binaryTypes, propertyName, from, to)));
	} catch (final Exception ex) {
	    ex.printStackTrace();
	    getResponse().setEntity(restUtil.errorRepresentation("Could not process POST request:\n" + ex.getMessage()));
	}
    }
}

