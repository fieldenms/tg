package ua.com.fielden.platform.web.resources;

import java.io.File;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Put;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.utils.MiscUtilities;

/**
 * This resource should be associated with {@link Attachment}.
 *
 *  Its behaviour is similar to {@link EntityTypeResource} with the difference that new attachment instances are saved upon POST instead of PUT request, which is required to multi-request form processing.
 *
 *  POSTing a new attachments results in uploading of the associated file.
 *
 * @author TG Team
 *
 */
public class AttachmentTypeResource extends EntityTypeResource<Attachment> {

    /** A directory location where all attachments should be stored */
    private final String location;

    public AttachmentTypeResource(final String location, final IAttachment controller, final EntityFactory factory, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	super(controller, factory, restUtil, context, request, response);
	this.location = location;
    }

    /**
     * Accepts and processes a representation posted to the resource.
     *
     * Expects a multi-part request representing
     */
    @Put
    @Override
    public Representation put(final Representation entity) {
	if (entity != null) {
	    if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {

		// The Apache FileUpload project parses HTTP requests which
		// conform to RFC 1867, "Form-based File Upload in HTML". That
		// is, if an HTTP request is submitted using the POST method,
		// and with a content type of "multipart/form-data", then
		// FileUpload can parse that request, and get all uploaded files
		// as FileItem.

		// Create a factory for disk-based file items
		final DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(10485760); // 10Mb limit

		// Create a new file upload handler based on the Restlet
		// FileUpload extension that will parse Restlet requests and
		// generates FileItems.
		final RestletFileUpload upload = new RestletFileUpload(factory);

		try {
		    // Request is parsed by the handler which generates a list of FileItems
		    final List<FileItem> items = upload.parseRepresentation(entity);

		    if (items.size() != 3) {
			getResponse().setEntity(new StringRepresentation("Unexpected structure of the request.", MediaType.TEXT_PLAIN));
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		    } else {
			final String key =  MiscUtilities.convertToString(items.get(0).getInputStream());
			final String desc = MiscUtilities.convertToString(items.get(1).getInputStream());
			final File file = new File(location + "/" + key);
			items.get(2).write(file);
			Attachment attachment = getFactory().newEntity(Attachment.class, key, desc);
			attachment.setFile(file);
			attachment = getDao().save(attachment);

			try {
			    return getRestUtil().singleRepresentation(attachment);
			} catch (final Exception ex) {
			    return getRestUtil().errorRepresentation(ex);
			}
		    }
		} catch (final Exception ex) {
		    // The message of all thrown exception is sent back to client
		    return getRestUtil().errorRepresentation(ex);
		}
	    }
	} else {
	    // PUT request with no entity.
	    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	}
	return new StringRepresentation("no result");
    }
}
