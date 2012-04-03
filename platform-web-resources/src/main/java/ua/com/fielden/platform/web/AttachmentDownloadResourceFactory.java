package ua.com.fielden.platform.web;

import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import ua.com.fielden.platform.attachment.IAttachmentController2;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.resources.AttachmentDownloadResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation for instantiating {@link AttachmentDownloadResource}.
 *
 * @author TG Team
 *
 */
public class AttachmentDownloadResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;
    private final String location;

    /**
     * Instances of DAO and factory should be thread-safe as they are used by multiple instances of resources serving concurrent requests.
     *
     */
    public AttachmentDownloadResourceFactory(final String location, final Injector injector) {
	this.injector = injector;
	this.restUtil = new RestServerUtil(injector.getInstance(ISerialiser.class));
	this.location = location;
    }

    @Override
    public void handle(final Request request, final Response response) {
	super.handle(request, response);

	if (Method.GET == request.getMethod()) {
	    new AttachmentDownloadResource(location, injector.getInstance(IAttachmentController2.class), restUtil, getContext(), request, response).handleGet();
	}
    }
}
