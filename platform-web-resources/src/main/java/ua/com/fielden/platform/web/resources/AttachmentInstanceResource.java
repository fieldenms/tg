package ua.com.fielden.platform.web.resources;

import java.io.File;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachmentController2;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.roa.HttpHeaders;

/**
 * Attachment instance resource, which has a specific deletion implementation.
 *
 * @author TG Team
 */
public class AttachmentInstanceResource extends EntityInstanceResource<Attachment> {

    private final String location;

    public AttachmentInstanceResource(final String location, final IAttachmentController2 dao, final EntityFactory factory, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	super(dao, factory, restUtil, context, request, response);
	this.location = location;
    }

    @Override
    public void handleDelete() {
	// need to delete file first and then persisted attachment instance from the database
	try {
	    final Attachment attachment = dao.findById(entityId);

	    final String fileName = location + "/" + attachment.getKey();
	    final File f = new File(fileName);

	    // make sure the file exists and isn't write protected
	    // if file does not exist then there is no need to delete it
	    if (f.exists()) {
		if (!f.canWrite()) {
		    throw new IllegalArgumentException("Delete: write protected " + fileName);
		}

		// Attempt to delete it
		final boolean success = f.delete();

		if (!success) {
		    throw new IllegalArgumentException("Delete: deletion failed");
		}
	    }
	    // if this line is reached then file was deleted successfully
	    dao.delete(attachment);
	} catch (final Exception ex) {
	    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	    restUtil.setHeaderEntry(getResponse(), HttpHeaders.ERROR, ex.getMessage());
	}
    }

}
