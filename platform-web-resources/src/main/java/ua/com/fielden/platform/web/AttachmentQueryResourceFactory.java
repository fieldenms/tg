package ua.com.fielden.platform.web;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.routing.Router;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachmentController;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.resources.EntityQueryResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation that provides logic for correct {@link Attachment} query resource instantiation.
 *
 * @author TG Team
 *
 */
public class AttachmentQueryResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;
    private final Router router;

    /**
     * Instances of DAO and factory should be thread-safe as they are used by multiple instances of resources serving concurrent requests.
     */
    public AttachmentQueryResourceFactory(final Injector injector, final Router router) {
	this.injector = injector;
	this.router = router;
	this.restUtil = new RestServerUtil(injector.getInstance(ISerialiser.class));
    }

    @Override
    public void handle(final Request request, final Response response) {
	super.handle(request, response);
	if (Method.POST.equals(request.getMethod())) {
	    final String username = (String) request.getAttributes().get("username");
	    injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserController.class));

	    new EntityQueryResource<Attachment>(router, injector, injector.getInstance(IAttachmentController.class), restUtil, getContext(), request, response).handle();
	}
    }
}
