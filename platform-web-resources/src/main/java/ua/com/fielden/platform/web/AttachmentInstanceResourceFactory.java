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
import ua.com.fielden.platform.web.resources.AttachmentInstanceResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * Factory for instantiating {@link Attachment} instance specific resource.
 *
 * @author TG Team
 *
 */
public class AttachmentInstanceResourceFactory extends Restlet {
    private final Injector injector;
    private final EntityFactory factory;
    private final RestServerUtil restUtil;
    private final String location;

    public AttachmentInstanceResourceFactory(final String location, final Injector injector, final EntityFactory factory) {
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

	final AttachmentInstanceResource resource = new AttachmentInstanceResource(location, dao, factory, restUtil, getContext(), request, response);
	if (Method.GET == request.getMethod() || Method.HEAD == request.getMethod() || Method.POST == request.getMethod() || Method.DELETE == request.getMethod()) {
	    resource.handle();
	}
    }
}
