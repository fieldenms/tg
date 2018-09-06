package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static ua.com.fielden.platform.attachment.Attachment.pn_SHA1;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;

import org.apache.log4j.Logger;
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
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * This resource should be used for downloading files that are associated with {@link Attachment} instances.
 * 
 * @author TG Team
 * 
 */
public class AttachmentDownloadResource extends ServerResource {
    private static final Logger LOGGER = Logger.getLogger(AttachmentDownloadResource.class);

    private final RestServerUtil restUtil;
    private final IAttachment coAttachment;
    private final Long attachmentId;
    private final String attachmentSha1;

    public AttachmentDownloadResource(
            final RestServerUtil restUtil,
            final IAttachment coAttachment, 
            final Context context, 
            final Request request, 
            final Response response) {
        init(context, request, response);
        this.restUtil = restUtil;
        this.coAttachment = coAttachment;
        this.attachmentId = Long.parseLong(request.getAttributes().get("attachment-id").toString());
        this.attachmentSha1 = request.getAttributes().get("attachment-sha1") + "";
    }

    @Get
    public Representation download() {
        return handleUndesiredExceptions(getResponse(), () -> {
            LOGGER.debug(format("Looking for attachment [ID = %s, SHA1=%s]", attachmentId, attachmentSha1));
            final EntityResultQueryModel<Attachment> query = select(Attachment.class).where().prop(ID).eq().val(attachmentId).and().prop(pn_SHA1).eq().val(attachmentSha1).model();
            return coAttachment.getEntityOptional(from(query).with(coAttachment.getFetchProvider().fetchModel()).model())
            .map(attachment -> {
                LOGGER.debug(format("Attachment [SHA1=%s] was found, getting the associated file.", attachmentId));
                return coAttachment.asFile(attachment)
                .map(file -> {                
                    LOGGER.debug(format("Preparing and sending the response with file for attachment [ID=%s].", attachmentId));
                    final MediaType fileType = MediaType.valueOf(attachment.getMime());    
                    final Disposition disposition = new Disposition();
                    disposition.setType(Disposition.TYPE_ATTACHMENT);
                    disposition.setFilename(attachment.getOrigFileName());
                    disposition.setModificationDate(attachment.getLastModified());
    
                    final Representation repFile = new FileRepresentation(file, fileType);
                    repFile.setDisposition(disposition);
                    return repFile;
                }).orElseThrow(() -> failure(format("Could not read file for attachment [ID = %s, SHA1=%s, %s]", attachmentId, attachmentSha1, attachment)));
            }).orElseThrow(() -> failure(format("Attachment [ID = %s, SHA1=%s] could not be found.", attachmentId, attachmentSha1)));
        }, restUtil);
    }
}
