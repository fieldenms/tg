package ua.com.fielden.platform.web;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.resources.AttachmentTypeResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation that provides logic for correct {@link Attachment} resource instantiation. *
 *
 * @author TG Team
 *
 */
public class AttachmentTypeResourceFactory extends Restlet {
    private final Injector injector;
    private final EntityFactory factory;
    private final RestServerUtil restUtil;
    private final String location;

    /**
     * Instances of DAO and factory should be thread-safe as they are used by multiple instances of resources serving concurrent requests.
     *
     * @param dao
     * @param factory
     */
    public AttachmentTypeResourceFactory(final String location, final Injector injector, final EntityFactory factory) {
	this.injector = injector;
	this.factory = factory;
	this.restUtil = new RestServerUtil(injector.getInstance(ISerialiser.class));
	this.location = location;
    }

    @Override
    public void handle(final Request request, final Response response) {
	super.handle(request, response);

	final IAttachment dao = injector.getInstance(IAttachment.class);

	final String username = (String) request.getAttributes().get("username");
	injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserController.class));
	// Method.GET.equals(request.getMethod()) || Method.HEAD.equals(request.getMethod()) ||
	if (Method.PUT.equals(request.getMethod())) {
	    new AttachmentTypeResource(location, dao, factory, restUtil, getContext(), request, response).handle();
	}
    }
}
