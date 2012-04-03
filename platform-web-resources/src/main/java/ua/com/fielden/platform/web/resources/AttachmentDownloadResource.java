package ua.com.fielden.platform.web.resources;

import java.io.File;
import java.io.FileInputStream;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachmentController2;

/**
 * Represents a web resource for downloading a file associated with an instance of {@link Attachment}.
 *
 * @author TG Team
 */
public class AttachmentDownloadResource extends Resource {
    // the following properties are determined from request
    private final String username;

    private final IAttachmentController2 controller;
    private final RestServerUtil restUtil;

    private final Long attachmentId;
    private final String location;

    public AttachmentDownloadResource(final String location, final IAttachmentController2 controller, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	super(context, request, response);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));

	this.location = location;
	this.controller = controller;
	this.restUtil = restUtil;
	this.username = (String) request.getAttributes().get("username");
	controller.setUsername(username);

	attachmentId = Long.parseLong(request.getAttributes().get("entity-id").toString());
    }

    // //////////////////////////////////////////////////////////////////
    // let's specify what HTTP methods are supported by this resource //
    // //////////////////////////////////////////////////////////////////
    @Override
    public boolean allowPost() {
	return false;
    }

    @Override
    public boolean allowGet() {
	return true;
    }


    /**
     * Handles GET requests resulting from RAO implementation of IAttachmentController.download
     */
    @Override
    public Representation represent(final Variant variant) {
	// ensure that request media type is supported
	if (!MediaType.APPLICATION_OCTET_STREAM.equals(variant.getMediaType())) {
	    return restUtil.errorRepresentation("Unsupported media type " + variant.getMediaType() + ".");
	}
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
