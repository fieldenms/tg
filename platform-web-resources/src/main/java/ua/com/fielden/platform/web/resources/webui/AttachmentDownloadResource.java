package ua.com.fielden.platform.web.resources.webui;

import java.io.File;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Disposition;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * This resource should be used for downloading files that are associated with {@link Attachment} instances.
 * 
 * @author TG Team
 * 
 */
public class AttachmentDownloadResource extends ServerResource {
    private static final Logger LOGGER = Logger.getLogger(AttachmentDownloadResource.class);

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
        final Optional<Attachment> opAttachment = coAttachment.findByIdOptional(attachmentId, coAttachment.getFetchProvider().fetchModel());
        return opAttachment
               .map(attachment -> {
                    try {
                        final File file = coAttachment.asFile(attachment);
                        final MediaType fileType = MediaType.valueOf(attachment.getMime());

                        final Disposition disposition = new Disposition();
                        disposition.setType(Disposition.TYPE_ATTACHMENT);
                        disposition.setFilename(attachment.getOrigFileName());
                        disposition.setModificationDate(attachment.getLastModified());

                        final Representation repFile = new FileRepresentation(file, fileType);
                        repFile.setDisposition(disposition);

                        return repFile;
                    } catch (final Exception ex) {
                        LOGGER.fatal(ex);
                        return this.errorPage();
                    }
                })
               .orElseGet(this::errorPage);
    }
    
    private Representation errorPage() {
        getResponse().setStatus(Status.CLIENT_ERROR_GONE);
        try {
            final String errorPageTemplate = ResourceLoader.getText("ua/com/fielden/platform/web/error-page-template.html")
                    .replaceAll("@title", "Error")
                    .replaceAll("@error-heading", "Error")
                    .replaceAll("@error-subheading", "Attachment could not be found.");
            
            return new EncodeRepresentation(Encoding.GZIP, new StringRepresentation(errorPageTemplate, MediaType.TEXT_HTML));
        } catch (final Exception ex) {
            LOGGER.fatal(ex);
            throw new IllegalStateException(ex);
        }
    }
}
