package ua.com.fielden.platform.web.resources.webui;

import java.io.File;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachment;

/**
 * This resource should be used for uploading files to be processed with the specified functional entity.
 * 
 * Unlike {@link AttachmentTypeResource} it does not save or associated the uploaded file with any entity. Instead it passes that file into
 * 
 * @author TG Team
 * 
 */
public class AttachmentDownloadResource extends ServerResource {

    private final IAttachment coAttachment;
    private final Long attachmentId;

    public AttachmentDownloadResource(
            final IAttachment coAttachment, 
            final Context context, 
            final Request request, 
            final Response response) {
        init(context, request, response);
        this.coAttachment = coAttachment;
        this.attachmentId = Long.parseLong(request.getAttributes().get("attachment-id").toString());
    }

    @Get
    public Representation download() {
        final Attachment attachment = coAttachment.findById(attachmentId, coAttachment.getFetchProvider().fetchModel());
        
        final File file = coAttachment.asFile(attachment);
        final MediaType fileType = MediaType.valueOf(attachment.getMime());
        
        final Disposition disposition = new Disposition();
        disposition.setType(Disposition.TYPE_ATTACHMENT);
        disposition.setFilename(attachment.getOrigFileName());
        disposition.setModificationDate(attachment.getLastModified());
        
        final FileRepresentation repFile = new FileRepresentation(file, fileType);
        repFile.setDisposition(disposition);
        
        return repFile;
    }
}
