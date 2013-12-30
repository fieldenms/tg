package ua.com.fielden.platform.web.resources;

import java.io.File;
import java.io.FileInputStream;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachment;

/**
 * Represents a web resource for downloading a file associated with an instance of {@link Attachment}.
 *
 * @author TG Team
 */
public class AttachmentDownloadResource extends ServerResource {

    private final IAttachment controller;
    private final RestServerUtil restUtil;

    private final Long attachmentId;
    private final String location;

    public AttachmentDownloadResource(final String location, final IAttachment controller, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	init(context, request, response);
	setNegotiated(false);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));

	this.location = location;
	this.controller = controller;
	this.restUtil = restUtil;

	attachmentId = Long.parseLong(request.getAttributes().get("entity-id").toString());
    }

    /**
     * Handles GET requests resulting from RAO implementation of IAttachmentController.download
     */
    @Get
    @Override
    public Representation get() {
	// process GET request
	try {
	    //return restUtil.singleRepresentation(dao.findById(entityId));
	    final Attachment attachment = controller.findById(attachmentId);
	    final File file = new File(location + "/" + attachment.getKey());
	    if (file.canRead()) {
		return new InputRepresentation(new FileInputStream(file), MediaType.APPLICATION_OCTET_STREAM);
	    } else {
		getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		return restUtil.errorRepresentation("Could not read file " + attachment.getKey());
	    }
	} catch (final Exception ex) {
	    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	    return restUtil.errorRepresentation("Could not process GET request:\n" + ex.getMessage());
	}
    }
}
